package modernmods.modernfoundry.smeltery.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.modernfoundry.smeltery.block.entity.ILegacyCapabilityBlockEntity;
import modernmods.mantle.inventory.SmartItemHandlerSlot;
import modernmods.modernfoundry.shared.inventory.TriggeringBaseContainerMenu;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;

/**
 * Container for a block with a single item inventory
 */
public class SingleItemContainerMenu extends TriggeringBaseContainerMenu<BlockEntity> {
  public SingleItemContainerMenu(int id, @Nullable Inventory inv, @Nullable BlockEntity te) {
    super(TinkerSmeltery.singleItemContainer.get(), id, inv, te);
    if (te != null) {
      // read the inventory via the legacy IItemHandler capability directly; the new Capabilities.Item.BLOCK provider is
      // deferred (returns null) because the block entity's handler cannot yet be exposed as a ResourceHandler
      if (te instanceof ILegacyCapabilityBlockEntity legacy) {
        IItemHandler handler = legacy.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler != null) {
          this.addSlot(new SmartItemHandlerSlot(handler, 0, 80, 20));
        }
      }
      this.addInventorySlots();
    }
  }

  public SingleItemContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, BlockEntity.class));
  }

  @Override
  protected int getInventoryYOffset() {
    return 51;
  }
}
