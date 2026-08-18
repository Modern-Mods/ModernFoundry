package modernmods.modernfoundry.smeltery.block.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import modernmods.mantle.block.InventoryBlock;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.mantle.fluid.FluidTransferHelper;
import modernmods.modernfoundry.library.fluid.IFluidTankUpdater;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.entity.tank.ProxyItemTank;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/** Block entity with a tank that proxies to the nested item handler */
public class ProxyTankBlockEntity extends MantleBlockEntity implements IFluidTankUpdater, ILegacyCapabilityBlockEntity {
  /** Direct access to the fluid handler and item handler */
  @Getter
  private final ProxyItemTank<ProxyTankBlockEntity> itemTank = new ProxyItemTank<>(this);
  /** Capability instance for the item handler */
  private final LazyOptional<ProxyItemTank<?>> itemCapability = LazyOptional.of(() -> itemTank);
  /** Capability instance for the fluid handler */
  private final LazyOptional<net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource>> fluidCapability = LazyOptional.of(itemTank::getFluidHandler);
  /** Last comparator strength to reduce block updates */
  private int lastStrength = -1;
  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    super.preRemoveSideEffects(pos, state);
    // drop the stored container item when the block is removed (formerly Block#onRemove). Runs server-side before the block entity is removed
    if (this.level != null) {
      InventoryBlock.dropInventoryItems(this.level, pos, itemTank);
    }
  }

  protected ProxyTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  public ProxyTankBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.proxyTank.get(), pos, state);
  }


  /* Capability */

  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER) {
      return itemCapability.cast();
    }
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      return fluidCapability.cast();
    }
    return modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional.empty();
  }

  public void invalidateCaps() {
    itemCapability.invalidate();
    fluidCapability.invalidate();
  }


  /* Comparators */

  /**
   * Gets the current comparator strength for the tank
   * @return  Tank comparator strength
   */
  private int calculateComparatorStrength() {
    net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> handler = itemTank.getFluidHandler();
    if (handler.size() == 0) {
      return 0;
    }
    int capacity = handler.getCapacityAsInt(0, handler.getResource(0));
    if (capacity == 0) {
      return 0;
    }
    return 1 + 14 * handler.getAmountAsInt(0) / capacity;
  }

  /** Gets the current comparator strength */
  public int getComparatorStrength() {
    if (lastStrength == -1) {
      lastStrength = calculateComparatorStrength();
    }
    return lastStrength;
  }

  @Override
  public void onTankContentsChanged() {
    if (level != null && !level.isClientSide()) {
      setChangedFast();
      int newStrength = calculateComparatorStrength();
      if (newStrength != lastStrength) {
        lastStrength = newStrength;
        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
      }
    }
  }

  /* Interaction */

  /** Called when a player interacts with the fluid cannon */
  public void interact(Player player, InteractionHand hand, boolean clickedTank) {
    // skip client side
    if (level == null || level.isClientSide()) {
      return;
    }

    // transfer fluid if clicked tank
    ItemStack held = player.getItemInHand(hand);
    ItemStack inventory = itemTank.getStack();
    // if we have an active tank, try interacting
    if (!inventory.isEmpty()) {
      // must have a held item to interact
      if (!held.isEmpty() && FluidTransferHelper.interactWithContainer(level, worldPosition, itemTank.getFluidHandler(), player, hand).didTransfer()
        || FluidTransferHelper.interactWithFilledBucket(level, worldPosition, itemTank.getFluidHandler(), player, hand, getBlockState().getValue(HORIZONTAL_FACING)).didTransfer()) {
        return;
      }
      // if we clicked the tank, don't try and swap items unless we have no tank
      // if we clicked the item, then we probably want it removed
      if (clickedTank) {
        return;
      }
    }
    // no fluid transfer? swap items around
    // inventory is empty means place item inside
    if (inventory.isEmpty()) {
      if (!held.isEmpty() && itemTank.isItemValid(0, held)) {
        // split the stack to place into inventory
        ItemStack stack = held.split(itemTank.getSlotLimit(0));
        player.setItemInHand(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        itemTank.setStack(stack);
      }
      // if not holding anything, pick up the stack
    } else if (held.isEmpty()) {
      player.setItemInHand(hand, inventory);
      itemTank.setStack(ItemStack.EMPTY);
    } else {
      // the proxy tank prefers you giving it the same instance it had before on no change
      player.addItem(inventory);
      itemTank.setStack(inventory);
    }
  }


  /* NBT */
  private static final String TAG_ITEM = "item";

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    input.read(TAG_ITEM, CompoundTag.CODEC).ifPresent(itemTank::readFromNBT);
  }

  @Override
  protected void saveSynced(ValueOutput output) {
    super.saveSynced(output);
    output.store(TAG_ITEM, CompoundTag.CODEC, itemTank.writeToNBT());
  }
}
