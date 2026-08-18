package modernmods.modernfoundry.library.fluid;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.smeltery.network.FluidUpdatePacket;

/**
 * Single fluid tank backed by the 26.1 {@link net.neoforged.neoforge.transfer.ResourceHandler} API, exposing the legacy
 * fill/drain/getFluid convenience methods used throughout Tinkers on top of the transactional handler.
 */
public class FluidTankBase<T extends MantleBlockEntity> extends FluidStacksResourceHandler implements IFluidHandler, IFluidTank {

  protected T parent;

  public FluidTankBase(int capacity, T parent) {
    super(1, capacity);
    this.parent = parent;
  }

  /** Gets the fluid stored in the tank. Note the returned stack is the backing instance, treat as read-only. */
  public FluidStack getFluid() {
    return this.stacks.get(0);
  }

  /** Gets the amount of fluid stored in the tank */
  public int getFluidAmount() {
    return this.stacks.get(0).getAmount();
  }

  /** Gets the max capacity of the tank */
  public int getCapacity() {
    return this.capacity;
  }

  /** Updates the tank capacity */
  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  /** Checks if the tank is empty */
  public boolean isEmpty() {
    return this.stacks.get(0).isEmpty();
  }

  /** Gets the remaining space in the tank */
  public int getSpace() {
    return Math.max(0, this.capacity - getFluidAmount());
  }

  /** Checks if the given fluid may be stored in the tank */
  public boolean isFluidValid(FluidStack stack) {
    return isValid(0, FluidResource.of(stack));
  }

  /** Directly sets the fluid in the tank, bypassing capacity checks. Used when syncing from the server. */
  public void setFluid(FluidStack stack) {
    FluidStack old = this.stacks.get(0);
    this.stacks.set(0, stack.copy());
    onContentsChanged(0, old);
  }

  /** Fills the tank with the given fluid, returning the amount filled */
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

  /** Drains up to the given amount of fluid from the tank */
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

  /** Drains the given fluid from the tank if it matches the stored fluid */
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !FluidResource.of(resource).matches(getFluid())) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  /* Legacy IFluidHandler style accessors */

  public int getTanks() {
    return 1;
  }

  public FluidStack getFluidInTank(int tank) {
    return getFluid();
  }

  public int getTankCapacity(int tank) {
    return this.capacity;
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return isFluidValid(stack);
  }

  @Override
  protected void onContentsChanged(int index, FluidStack previousContents) {
    onContentsChanged();
  }

  /** Notifies the parent block entity that the tank contents changed. Also invoked directly by some callers. */
  public void onContentsChanged() {
    if (parent instanceof IFluidTankUpdater updater) {
      updater.onTankContentsChanged();
    }

    parent.setChanged();
    Level level = parent.getLevel();
    if (level != null && !level.isClientSide()) {
      TinkerNetwork.getInstance().sendToClientsAround(new FluidUpdatePacket(parent.getBlockPos(), this.getFluid()), level, parent.getBlockPos());
    }
  }
}
