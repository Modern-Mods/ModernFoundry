package modernmods.modernfoundry.tables.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import modernmods.modernfoundry.tables.TinkerTables;
import modernmods.modernfoundry.tables.block.entity.chest.AbstractChestBlockEntity;
import modernmods.modernfoundry.tables.menu.module.SideInventoryContainer;

import javax.annotation.Nullable;

public class TinkerChestContainerMenu extends TabbedContainerMenu<AbstractChestBlockEntity> {
  protected SideInventoryContainer<AbstractChestBlockEntity> inventory;
  public TinkerChestContainerMenu(int id, Inventory inv, @Nullable AbstractChestBlockEntity tileEntity) {
    super(TinkerTables.tinkerChestContainer.get(), id, inv, tileEntity);
    // columns don't matter since they get set by gui
    if (this.tile != null) {
      this.inventory = new DynamicChestInventory(TinkerTables.tinkerChestContainer.get(), this.containerId, inv, this.tile, 8, 18, 8);
      this.addSubContainer(inventory, true);
    }
    this.addInventorySlots();
  }

  public TinkerChestContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, AbstractChestBlockEntity.class));
  }

  @Override
  protected int getInventoryYOffset() {
    return 102;
  }

  /** Resizable inventory */
  public static class DynamicChestInventory extends SideInventoryContainer<AbstractChestBlockEntity> {
    public DynamicChestInventory(MenuType<?> containerType, int windowId, Inventory inv, AbstractChestBlockEntity tile, int x, int y, int columns) {
      // pass the chest's own item handler directly instead of resolving it through the Capabilities.Item.BLOCK lookup:
      // the chest handler is a legacy IItemHandler that isn't (yet) exposed as a 26.1 ResourceHandler, so the cap lookup
      // returns null and the menu built zero slots. The smeltery side-inventory uses this same direct-handler path.
      super(containerType, windowId, inv, tile, tile == null ? null : tile.getItemHandler(), x, y, columns);
    }
  }
}
