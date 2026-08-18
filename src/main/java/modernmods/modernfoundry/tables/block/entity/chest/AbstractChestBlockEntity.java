package modernmods.modernfoundry.tables.block.entity.chest;

import net.minecraft.world.level.storage.ValueOutput;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Containers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import modernmods.mantle.block.entity.NameableBlockEntity;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.tables.block.ChestBlock;
import modernmods.modernfoundry.tables.block.entity.inventory.IChestItemHandler;
import modernmods.modernfoundry.tables.menu.TinkerChestContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Shared base logic for all Tinkers' chest tile entities */
public abstract class AbstractChestBlockEntity extends NameableBlockEntity {
  private static final String KEY_ITEMS = "Items";

  @Getter
  private final IChestItemHandler itemHandler;
  private final LazyOptional<IItemHandler> capability;
  protected AbstractChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name, IChestItemHandler itemHandler) {
    super(type, pos, state, name);
    itemHandler.setParent(this);
    this.itemHandler = itemHandler;
    this.capability = LazyOptional.of(() -> itemHandler);
  }

  @Nonnull
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER) {
      return capability.cast();
    }
    return modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional.empty(); // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent
  }

  public void invalidateCaps() {
    // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent (was super.invalidateCaps();)
    capability.invalidate();
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new TinkerChestContainerMenu(menuId, playerInventory, this);
  }

  /**
   * Checks if the given item should be inserted into the chest on interact
   * @param player    Player inserting
   * @param heldItem  Stack to insert
   * @return  Return true
   */
  public boolean canInsert(Player player, ItemStack heldItem) {
    return true;
  }

  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    itemHandler.serialize(output);
  }

  /** Reads the inventory from NBT */
  public void readInventory(CompoundTag tags) {
    // copy in just the items key for deserializing, don't want to change the size
    CompoundTag handlerNBT = new CompoundTag();
    handlerNBT.put(KEY_ITEMS, tags.getListOrEmpty(KEY_ITEMS));
    ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, TagUtil.BUILTIN_LOOKUP, handlerNBT);
    itemHandler.deserialize(input);
  }

  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    itemHandler.deserialize(input);
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    super.preRemoveSideEffects(pos, state);
    // chests flagged to drop items scatter their contents when broken; the rest preserve them in the dropped block item via loot
    if (level != null && state.getBlock() instanceof ChestBlock chest && chest.dropsItems()) {
      for (int i = 0; i < itemHandler.getSlots(); i++) {
        ItemStack stack = itemHandler.getStackInSlot(i);
        if (!stack.isEmpty()) {
          Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
      }
    }
  }
}
