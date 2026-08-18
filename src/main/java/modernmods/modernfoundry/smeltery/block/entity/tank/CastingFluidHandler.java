package modernmods.modernfoundry.smeltery.block.entity.tank;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import modernmods.mantle.compat.neoforged.neoforge.registries.ForgeRegistries;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.smeltery.block.entity.CastingBlockEntity;
import modernmods.modernfoundry.smeltery.item.TankItem;

import javax.annotation.Nonnull;

/**
 * Fluid handler for a casting table/basin. Backed by the 26.1 {@link ResourceHandler} transfer API (so it registers as a
 * {@code ResourceHandler<FluidResource>} capability) while keeping the legacy {@link IFluidHandler} for internal callers.
 *
 * <p>The casting recipe capacity is determined dynamically the first time fluid is inserted; {@link CastingBlockEntity#initNewCasting}
 * has a side-effect-free SIMULATE mode used to size the tank inside a transaction, while the actual recipe lock in
 * (and the tile change/reset notifications) are non-transactional and are only applied once the transaction is committed.
 */
public class CastingFluidHandler implements ResourceHandler<FluidResource>, IFluidHandler {
  private final CastingBlockEntity tile;
  private FluidStack fluid = FluidStack.EMPTY;
  private int capacity = 0;
  private Fluid filter = Fluids.EMPTY;

  /** Snapshot of the handler state, for transactional rollback */
  private record Snapshot(FluidStack fluid, int capacity, Fluid filter) {}

  /** Journal tracking transactional changes to the tank; defers the non-transactional tile side effects to commit */
  private final SnapshotJournal<Snapshot> journal = new SnapshotJournal<>() {
    @Override
    protected Snapshot createSnapshot() {
      return new Snapshot(fluid.copy(), capacity, filter);
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
      fluid = snapshot.fluid();
      capacity = snapshot.capacity();
      filter = snapshot.filter();
    }

    @Override
    protected void onRootCommit(Snapshot original) {
      if (fluid.isEmpty() && !original.fluid().isEmpty()) {
        // drained to empty: reset the casting recipe (also clears this tank via tile.reset -> tank.reset)
        tile.reset();
      } else {
        // if the casting recipe was initialized during this transaction, lock it in now (real execution path)
        if (original.filter() == Fluids.EMPTY && filter != Fluids.EMPTY) {
          tile.initNewCasting(fluid, FluidAction.EXECUTE);
        }
        tile.onContentsChanged();
      }
    }
  };

  public CastingFluidHandler(CastingBlockEntity tile) {
    this.tile = tile;
  }

  /** Gets the fluid in the tank */
  public FluidStack getFluid() {
    return fluid;
  }

  /** Sets the fluid in the tank directly, used by the tile when pouring */
  public void setFluid(FluidStack fluid) {
    this.fluid = fluid;
  }

  /** Sets the tank capacity directly */
  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  /** Checks if the given fluid is valid */
  public boolean isFluidValid(FluidStack stack) {
    return !stack.isEmpty() && (filter == Fluids.EMPTY || stack.getFluid() == filter);
  }

  /** Checks if the fluid is empty */
  public boolean isEmpty() {
    return fluid.isEmpty();
  }

  /** Gets the current capacity of this fluid handler */
  public int getCapacity() {
    if (capacity == 0) {
      return fluid.getAmount();
    }
    return capacity;
  }

  /** Resets the tanks filter */
  public void reset() {
    capacity = 0;
    fluid = FluidStack.EMPTY;
    filter = Fluids.EMPTY;
  }


  /* ResourceHandler */

  @Override
  public int size() {
    return 1;
  }

  @Override
  public FluidResource getResource(int index) {
    return FluidResource.of(fluid);
  }

  @Override
  public long getAmountAsLong(int index) {
    return fluid.getAmount();
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
    FluidStack resourceStack = resource.toStack(amount);
    if (!isFluidValid(resourceStack)) {
      return 0;
    }
    // determine the recipe capacity, initializing the casting if not yet done (SIMULATE has no side effects)
    int cap = this.capacity;
    boolean initializing = filter == Fluids.EMPTY || this.capacity == 0;
    if (initializing) {
      cap = tile.initNewCasting(resourceStack, FluidAction.SIMULATE);
      if (cap <= 0) {
        return 0;
      }
    }
    // compute how much actually fits
    int filled;
    if (fluid.isEmpty()) {
      filled = Math.min(cap, amount);
    } else if (!FluidStack.isSameFluidSameComponents(fluid, resourceStack)) {
      return 0;
    } else {
      filled = Math.min(cap - fluid.getAmount(), amount);
    }
    if (filled <= 0) {
      return 0;
    }
    // apply the change, snapshotting so it can be rolled back
    journal.updateSnapshots(transaction);
    if (initializing) {
      this.capacity = cap;
      this.filter = resourceStack.getFluid();
    }
    if (fluid.isEmpty()) {
      this.fluid = resourceStack.copyWithAmount(filled);
    } else {
      this.fluid = fluid.copyWithAmount(fluid.getAmount() + filled);
    }
    return filled;
  }

  @Override
  public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
    if (index != 0 || resource.isEmpty() || amount <= 0) {
      return 0;
    }
    if (fluid.isEmpty() || !FluidStack.isSameFluidSameComponents(fluid, resource.toStack(1))) {
      return 0;
    }
    int drained = Math.min(fluid.getAmount(), amount);
    if (drained <= 0) {
      return 0;
    }
    journal.updateSnapshots(transaction);
    this.fluid = fluid.copyWithAmount(fluid.getAmount() - drained);
    return drained;
  }


  /* Legacy IFluidHandler */

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    try (Transaction tx = Transaction.openRoot()) {
      int filled = insert(0, FluidResource.of(resource), resource.getAmount(), tx);
      if (action.execute()) {
        tx.commit();
      }
      return filled;
    }
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(fluid, resource)) {
      return FluidStack.EMPTY;
    }
    return this.drain(resource.getAmount(), action);
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    if (fluid.isEmpty() || maxDrain <= 0) {
      return FluidStack.EMPTY;
    }
    FluidStack current = fluid;
    try (Transaction tx = Transaction.openRoot()) {
      int drained = extract(0, FluidResource.of(current), maxDrain, tx);
      if (action.execute()) {
        tx.commit();
      }
      return drained > 0 ? current.copyWithAmount(drained) : FluidStack.EMPTY;
    }
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    if (tank == 0) {
      return fluid;
    }
    return FluidStack.EMPTY;
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
  public boolean isFluidValid(int tank, FluidStack stack) {
    return tank == 0 && isFluidValid(stack);
  }

  /* Tag */
  private static final String TAG_FLUID = "fluid";
  private static final String TAG_FILTER = "filter";
  private static final String TAG_CAPACITY = "capacity";

  /** Reads the tank from Tag */
  public void readFromTag(CompoundTag nbt) {
    capacity = nbt.getIntOr(TAG_CAPACITY, 0);
    if (nbt.contains(TAG_FLUID)) {
      CompoundTag fluidTag = nbt.getCompoundOrEmpty(TAG_FLUID);
      setFluid(fluidTag.contains("FluidName") ? TankItem.readFluid(fluidTag) : TagUtil.readFluid(fluidTag));
    }
    if (nbt.contains(TAG_FILTER)) {
      Identifier id = Identifier.tryParse(nbt.getStringOr(TAG_FILTER, ""));
      Fluid fluid = id == null ? null : ForgeRegistries.FLUIDS.getValue(id);
      if (fluid != null) {
        filter = fluid;
      }
    }
  }

  /** Write the tank from NBT */
  @SuppressWarnings("deprecation")
  public CompoundTag writeToTag(CompoundTag nbt) {
    nbt.putInt(TAG_CAPACITY, capacity);
    if (!fluid.isEmpty()) {
      nbt.put(TAG_FLUID, TagUtil.writeFluid(fluid));
    }
    if (filter != Fluids.EMPTY) {
      nbt.putString(TAG_FILTER, BuiltInRegistries.FLUID.getKey(filter).toString());
    }
    return nbt;
  }
}
