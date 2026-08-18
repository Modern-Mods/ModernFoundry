package modernmods.modernfoundry.smeltery.block.entity.tank;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.fluid.IMultitankListChange;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.library.utils.WeakListenerList;
import modernmods.modernfoundry.smeltery.block.entity.tank.ISmelteryTankHandler.FluidChange;
import modernmods.modernfoundry.smeltery.item.TankItem;
import modernmods.modernfoundry.smeltery.network.SmelteryTankUpdatePacket;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluid handler implementation for the smeltery.
 *
 * <p>Backed by the 26.1 {@link ResourceHandler} transfer API (so it registers as a {@code ResourceHandler<FluidResource>}
 * capability) while keeping the legacy {@link IFluidHandler}. The tank stores a dynamic list of fluids; a
 * {@link SnapshotJournal} captures the list and total for transactional rollback, and the non-transactional listener
 * notifications ({@link ISmelteryTankHandler#notifyFluidsChanged} and the {@link #tankListChange} listeners) are
 * reconstructed from the before/after diff and deferred to {@link SnapshotJournal#onRootCommit}.
 */
public class SmelteryTank<T extends MantleBlockEntity & ISmelteryTankHandler> implements ResourceHandler<FluidResource>, IFluidHandler, IMultitankListChange {
  private final T parent;
  /** Fluids actually contained in the tank */
  @Getter
  private final List<FluidStack> fluids;
  /** Maximum capacity of the smeltery */
  private int capacity;
  /** Current amount of fluid in the tank */
  @Getter
  private int contained;
  /** Listener for the tank list changing */
  private final WeakListenerList tankListChange = new WeakListenerList();

  /** Snapshot of the mutable tank state for transactional rollback */
  private record Snapshot(List<FluidStack> fluids, int contained) {}

  /** Journal tracking transactional changes; reconstructs and defers the listener notifications to commit */
  private final SnapshotJournal<Snapshot> journal = new SnapshotJournal<>() {
    @Override
    protected Snapshot createSnapshot() {
      return new Snapshot(new ArrayList<>(fluids), contained);
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
      fluids.clear();
      fluids.addAll(snapshot.fluids());
      contained = snapshot.contained();
    }

    @Override
    protected void onRootCommit(Snapshot original) {
      List<FluidStack> before = original.fluids();
      boolean structural = before.size() != fluids.size();
      // fire ADDED for fluids that are new (e.g. so alloy recipe caches refresh)
      for (FluidStack now : fluids) {
        if (before.stream().noneMatch(old -> FluidStack.isSameFluidSameComponents(old, now))) {
          parent.notifyFluidsChanged(FluidChange.ADDED, now);
        }
      }
      // fire REMOVED for fluids that are gone
      for (FluidStack old : before) {
        if (fluids.stream().noneMatch(now -> FluidStack.isSameFluidSameComponents(now, old))) {
          parent.notifyFluidsChanged(FluidChange.REMOVED, old);
        }
      }
      // fire CHANGED to queue the client update / setChanged for amount changes
      parent.notifyFluidsChanged(FluidChange.CHANGED, getFluidInTank(0));
      // run the list listeners on a structural or fullness change
      boolean wasFull = original.contained() >= capacity;
      boolean isFull = contained >= capacity;
      if (structural || wasFull != isFull) {
        tankListChange.run();
      }
    }
  };

  public SmelteryTank(T parent) {
    fluids = Lists.newArrayList();
    capacity = 0;
    contained = 0;
    this.parent = parent;
  }

  /**
   * Called when the fluids change to sync to client
   */
  public void syncFluids() {
    Level world = parent.getLevel();
    if (world != null && !world.isClientSide()) {
      BlockPos pos = parent.getBlockPos();
      TinkerNetwork.getInstance().sendToClientsAround(new SmelteryTankUpdatePacket(pos, fluids), world, pos);
    }
  }


  /* Capacity and space */

  /**
   * Updates the maximum tank capacity
   * @param maxCapacity  New max capacity
   */
  public void setCapacity(int maxCapacity) {
    this.capacity = maxCapacity;
  }

  /**
   * Gets the maximum amount of space in the smeltery tank
   * @return  Tank capacity
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Gets the amount of empty space in the tank
   * @return  Remaining space in the tank
   */
  public int getRemainingSpace() {
    if (contained >= capacity) {
      return 0;
    }
    return capacity - contained;
  }


  /* Fluids */

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return true;
  }

  @Override
  public int getTanks() {
    if (contained < capacity) {
      return fluids.size() + 1;
    }
    return fluids.size();
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    if (tank < 0 || tank >= fluids.size()) {
      return FluidStack.EMPTY;
    }
    return fluids.get(tank);
  }

  @Override
  public int getTankCapacity(int tank) {
    if (tank < 0) {
      return 0;
    }
    // index of the tank size means the "empty" segment
    int remaining = capacity - contained;
    if (tank == fluids.size()) {
      return remaining;
    }
    if (tank > fluids.size()) {
      return 0;
    }
    // any valid index, return the amount contained and the extra space
    return fluids.get(tank).getAmount() + remaining;
  }

  /**
   * Moves the fluid with the passed index to the beginning/bottom of the fluid tank stack
   * @param index  Index to move
   */
  public void moveFluidToBottom(int index) {
    if (index < fluids.size()) {
      FluidStack fluid = fluids.get(index);
      fluids.remove(index);
      fluids.add(0, fluid);
      parent.notifyFluidsChanged(FluidChange.CHANGED, FluidStack.EMPTY);
      tankListChange.run();
    }
  }


  /* ResourceHandler */

  @Override
  public int size() {
    return getTanks();
  }

  @Override
  public FluidResource getResource(int index) {
    return FluidResource.of(getFluidInTank(index));
  }

  @Override
  public long getAmountAsLong(int index) {
    return getFluidInTank(index).getAmount();
  }

  @Override
  public long getCapacityAsLong(int index, FluidResource resource) {
    return getTankCapacity(index);
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    return true;
  }

  @Override
  public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0 || index < 0 || index > fluids.size() || contained >= capacity) {
      return 0;
    }
    int usable = Math.min(capacity - contained, amount);
    if (usable <= 0) {
      return 0;
    }
    FluidStack resourceStack = resource.toStack(1);
    // inserting into an existing slot requires a matching fluid
    if (index < fluids.size()) {
      FluidStack existing = fluids.get(index);
      if (!FluidStack.isSameFluidSameComponents(existing, resourceStack)) {
        return 0;
      }
      journal.updateSnapshots(transaction);
      contained += usable;
      fluids.set(index, existing.copyWithAmount(existing.getAmount() + usable));
      return usable;
    }
    // empty segment: grow a matching fluid if present, else append a new one
    for (int i = 0; i < fluids.size(); i++) {
      FluidStack existing = fluids.get(i);
      if (FluidStack.isSameFluidSameComponents(existing, resourceStack)) {
        journal.updateSnapshots(transaction);
        contained += usable;
        fluids.set(i, existing.copyWithAmount(existing.getAmount() + usable));
        return usable;
      }
    }
    journal.updateSnapshots(transaction);
    contained += usable;
    fluids.add(resource.toStack(usable));
    return usable;
  }

  @Override
  public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0 || index < 0 || index >= fluids.size()) {
      return 0;
    }
    FluidStack fluid = fluids.get(index);
    if (fluid.isEmpty() || !FluidStack.isSameFluidSameComponents(fluid, resource.toStack(1))) {
      return 0;
    }
    int drained = Math.min(amount, fluid.getAmount());
    if (drained <= 0) {
      return 0;
    }
    journal.updateSnapshots(transaction);
    contained -= drained;
    if (fluid.getAmount() - drained <= 0) {
      fluids.remove(index);
    } else {
      fluids.set(index, fluid.copyWithAmount(fluid.getAmount() - drained));
    }
    return drained;
  }


  /* Legacy IFluidHandler */

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
  public FluidStack drain(int maxDrain, FluidAction action) {
    if (fluids.isEmpty() || maxDrain <= 0) {
      return FluidStack.EMPTY;
    }
    // drain the first fluid
    FluidStack first = fluids.get(0);
    try (Transaction tx = Transaction.openRoot()) {
      int drained = extract(0, FluidResource.of(first), maxDrain, tx);
      if (action.execute()) {
        tx.commit();
      }
      return drained > 0 ? first.copyWithAmount(drained) : FluidStack.EMPTY;
    }
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack toDrain, FluidAction action) {
    if (toDrain.isEmpty()) {
      return FluidStack.EMPTY;
    }
    // find the matching fluid index
    for (int i = 0; i < fluids.size(); i++) {
      if (FluidStack.isSameFluidSameComponents(fluids.get(i), toDrain)) {
        try (Transaction tx = Transaction.openRoot()) {
          int drained = extract(i, FluidResource.of(toDrain), toDrain.getAmount(), tx);
          if (action.execute()) {
            tx.commit();
          }
          return drained > 0 ? toDrain.copyWithAmount(drained) : FluidStack.EMPTY;
        }
      }
    }
    return FluidStack.EMPTY;
  }

  /* Saving and loading */

  private static final String TAG_FLUIDS = "fluids";
  private static final String TAG_CAPACITY = "capacity";

  /**
   * Updates fluids in the tank, typically from a packet
   * @param fluids  List of fluids
   */
  public void setFluids(List<FluidStack> fluids) {
    FluidStack oldFirst = getFluidInTank(0);
    this.fluids.clear();
    this.fluids.addAll(fluids);
    contained = fluids.stream().mapToInt(FluidStack::getAmount).reduce(0, Integer::sum);
    FluidStack newFirst = getFluidInTank(0);
    if (!FluidStack.isSameFluidSameComponents(oldFirst, newFirst)) {
      parent.notifyFluidsChanged(FluidChange.ORDER_CHANGED, newFirst);
      tankListChange.run();
    }
  }

  /** Writes the tank to NBT */
  public CompoundTag write(CompoundTag nbt) {
    ListTag list = new ListTag();
    for (FluidStack liquid : fluids) {
      list.add(TagUtil.writeFluid(liquid));
    }
    nbt.put(TAG_FLUIDS, list);
    nbt.putInt(TAG_CAPACITY, capacity);
    return nbt;
  }

  /** Reads the tank from NBT */
  public void read(CompoundTag tag) {
    ListTag list = tag.getListOrEmpty(TAG_FLUIDS);
    fluids.clear();
    contained = 0;
    for (int i = 0; i < list.size(); i++) {
      CompoundTag fluidTag = list.getCompoundOrEmpty(i);
      FluidStack fluid = fluidTag.contains("FluidName") ? TankItem.readFluid(fluidTag) : TagUtil.readFluid(fluidTag);
      if (!fluid.isEmpty()) {
        fluids.add(fluid);
        contained += fluid.getAmount();
      }
    }
    capacity = tag.getIntOr(TAG_CAPACITY, 0);
  }


  /* Listeners */

  @Override
  public <TE> void addTankListListener(TE parent, Consumer<TE> listener) {
    tankListChange.addListener(parent, listener);
  }

  @Override
  public void removeTankListListeners(Object parent) {
    tankListChange.removeListeners(parent);
  }
}
