package modernmods.modernfoundry.library.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;

/**
 * Base for a single fluid tank whose storage is provided externally through {@link #getFluid()}/{@link #setFluid(FluidStack)}
 * (for example backed by a tool's persistent data), rather than by an internal {@link FluidStack} field.
 *
 * <p>In MC 26.1 it is backed by the {@link ResourceHandler} transfer API so it can be handed to
 * {@code ResourceHandler<FluidResource>} consumers, while still implementing the legacy {@link IFluidHandler}/{@link IFluidTank}.
 * A {@link SnapshotJournal} snapshots the fluid (via get/set) for transactional rollback, and the non-transactional change
 * notification ({@link #onContentsChanged()}) is deferred to the commit of the root transaction.
 */
public abstract class SimpleFluidTank implements ResourceHandler<FluidResource>, IFluidTank, IFluidHandler {
  private final SnapshotJournal<FluidStack> journal = new SnapshotJournal<>() {
    @Override
    protected FluidStack createSnapshot() {
      return getFluid().copy();
    }

    @Override
    protected void revertToSnapshot(FluidStack snapshot) {
      setFluid(snapshot);
    }

    @Override
    protected void onRootCommit(FluidStack original) {
      onContentsChanged();
    }
  };

  /** Gets the fluid currently stored */
  @Nonnull
  @Override
  public abstract FluidStack getFluid();

  /** Called to set the fluid after it has changed */
  public abstract void setFluid(FluidStack fluid);

  @Override
  public abstract int getCapacity();

  /** Called after the tank contents change, once per committed transaction. Override to sync. */
  protected void onContentsChanged() {}


  /* Redirect duplicate methods */

  @Override
  public int getFluidAmount() {
    return getFluid().getAmount();
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return getFluid();
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public int getTankCapacity(int tank) {
    return getCapacity();
  }

  @Override
  public boolean isFluidValid(FluidStack stack) {
    return true;
  }

  @Override
  public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
    return isFluidValid(stack);
  }


  /* ResourceHandler */

  @Override
  public int size() {
    return 1;
  }

  @Override
  public FluidResource getResource(int index) {
    return FluidResource.of(getFluid());
  }

  @Override
  public long getAmountAsLong(int index) {
    return getFluid().getAmount();
  }

  @Override
  public long getCapacityAsLong(int index, FluidResource resource) {
    return getCapacity();
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    return index == 0 && isFluidValid(resource.toStack(1));
  }

  @Override
  public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
    if (index != 0 || resource.isEmpty() || amount <= 0) {
      return 0;
    }
    FluidStack resourceStack = resource.toStack(1);
    if (!isFluidValid(resourceStack)) {
      return 0;
    }
    FluidStack current = getFluid();
    int filled;
    if (current.isEmpty()) {
      filled = Math.min(getCapacity(), amount);
    } else if (!FluidStack.isSameFluidSameComponents(current, resourceStack)) {
      return 0;
    } else {
      filled = Math.min(getCapacity() - current.getAmount(), amount);
    }
    if (filled <= 0) {
      return 0;
    }
    journal.updateSnapshots(transaction);
    if (current.isEmpty()) {
      setFluid(resource.toStack(filled));
    } else {
      setFluid(current.copyWithAmount(current.getAmount() + filled));
    }
    return filled;
  }

  @Override
  public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
    if (index != 0 || resource.isEmpty() || amount <= 0) {
      return 0;
    }
    FluidStack current = getFluid();
    if (current.isEmpty() || !FluidStack.isSameFluidSameComponents(current, resource.toStack(1))) {
      return 0;
    }
    int drained = Math.min(current.getAmount(), amount);
    if (drained <= 0) {
      return 0;
    }
    journal.updateSnapshots(transaction);
    setFluid(current.copyWithAmount(current.getAmount() - drained));
    return drained;
  }


  /* Legacy filling and draining */

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    try (Transaction tx = Transaction.openRoot()) {
      int filled = insert(FluidResource.of(resource), resource.getAmount(), tx);
      if (action.execute()) {
        tx.commit();
      }
      return filled;
    }
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(getFluid(), resource)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    FluidStack current = getFluid();
    if (current.isEmpty() || maxDrain <= 0) {
      return FluidStack.EMPTY;
    }
    try (Transaction tx = Transaction.openRoot()) {
      int drained = extract(FluidResource.of(current), maxDrain, tx);
      if (action.execute()) {
        tx.commit();
      }
      return drained > 0 ? current.copyWithAmount(drained) : FluidStack.EMPTY;
    }
  }
}
