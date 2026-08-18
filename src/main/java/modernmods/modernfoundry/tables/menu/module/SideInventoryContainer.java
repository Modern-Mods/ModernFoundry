package modernmods.modernfoundry.tables.menu.module;

import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import modernmods.mantle.inventory.BaseContainerMenu;
import modernmods.mantle.inventory.EmptyItemHandler;
import modernmods.mantle.inventory.SmartItemHandlerSlot;

import javax.annotation.Nullable;

public class SideInventoryContainer<TILE extends BlockEntity> extends BaseContainerMenu<TILE> {

  @Getter
  private final int columns;
  @Getter
  private final int slotCount;
  protected final LazyOptional<IItemHandler> itemHandler;

  public SideInventoryContainer(MenuType<?> containerType, int windowId, Inventory inv, @Nullable TILE tile, int x, int y, int columns) {
    this(containerType, windowId, inv, tile, (Direction) null, x, y, columns);
  }

  /** Resolves the item handler from the block's {@link Capabilities#Item} capability, wrapping it as a legacy handler. */
  @Nullable
  private static IItemHandler resolveHandler(@Nullable BlockEntity tile, @Nullable Direction inventoryDirection) {
    if (tile == null) {
      return EmptyItemHandler.INSTANCE;
    }
    var level = tile.getLevel();
    var handlerRh = level == null ? null : level.getCapability(Capabilities.Item.BLOCK, tile.getBlockPos(), tile.getBlockState(), tile, inventoryDirection);
    return handlerRh == null ? null : IItemHandler.of(handlerRh);
  }

  public SideInventoryContainer(MenuType<?> containerType, int windowId, Inventory inv, @Nullable TILE tile, @Nullable Direction inventoryDirection, int x, int y, int columns) {
    this(containerType, windowId, inv, tile, resolveHandler(tile, inventoryDirection), x, y, columns);
  }

  /**
   * Constructor taking the item handler directly, bypassing the {@link Capabilities#Item} lookup. Used by block entities
   * that own their inventory (e.g. the smeltery), whose handler is a legacy {@link IItemHandler} and cannot be exposed as
   * a {@code ResourceHandler} without a full transfer-API port; the menu manipulates the slots directly regardless.
   */
  public SideInventoryContainer(MenuType<?> containerType, int windowId, Inventory inv, @Nullable TILE tile, @Nullable IItemHandler providedHandler, int x, int y, int columns) {
    super(containerType, windowId, inv, tile);
    this.itemHandler = LazyOptional.ofNullable(providedHandler);

    // slot properties
    IItemHandler handler = itemHandler.orElse(EmptyItemHandler.INSTANCE);
    this.slotCount = handler.getSlots();
    this.columns = Math.max(1, columns);
    int rows = this.slotCount / this.columns;
    if (this.slotCount % this.columns != 0) {
      rows++;
    }

    // add slots
    int index = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (index >= this.slotCount) {
          break;
        }

        this.addSlot(this.createSlot(handler, index, x + c * 18, y + r * 18));
        index++;
      }
    }
  }

  /**
   * Creates a slot for this inventory
   * @param itemHandler  Item handler
   * @param index        Slot index
   * @param x            Slot X position
   * @param y            Slot Y position
   * @return  Inventory slot
   */
  protected Slot createSlot(IItemHandler itemHandler, int index, int x, int y) {
    return new SmartItemHandlerSlot(itemHandler, index, x, y);
  }
}
