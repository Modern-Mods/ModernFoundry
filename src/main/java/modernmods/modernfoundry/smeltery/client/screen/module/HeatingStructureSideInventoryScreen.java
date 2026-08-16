package modernmods.modernfoundry.smeltery.client.screen.module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import modernmods.hilt.client.screen.ScalableElementScreen;
import modernmods.modernfoundry.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import modernmods.modernfoundry.smeltery.client.screen.HeatingStructureScreen;
import modernmods.modernfoundry.tables.client.inventory.module.SideInventoryScreen;
import modernmods.modernfoundry.tables.menu.module.SideInventoryContainer;

public class HeatingStructureSideInventoryScreen extends SideInventoryScreen<HeatingStructureScreen,SideInventoryContainer<? extends HeatingStructureBlockEntity>> {
  public static final ResourceLocation SLOT_LOCATION = HeatingStructureScreen.BACKGROUND;

  // TODO: read from a proper place
  public HeatingStructureSideInventoryScreen(HeatingStructureScreen parent, SideInventoryContainer<? extends HeatingStructureBlockEntity> container, Inventory playerInventory, int slotCount, int columns) {
    super(parent, container, playerInventory, Component.empty(), slotCount, columns, false, true);
    slot = new ScalableElementScreen(SLOT_LOCATION, 0, 238, 22, 18, 256, 256);
    slotEmpty = new ScalableElementScreen(SLOT_LOCATION, 22, 238, 22, 18, 256, 256);
    yOffset = 4;
  }

  @Override
  protected boolean shouldDrawName() {
    return false;
  }

  @Override
  protected void updateSlots() {
    // adjust for the heat bar
    xOffset += 4;
    super.updateSlots();
    xOffset -= 4;
  }

  @Override
  protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderTooltip(graphics, mouseX, mouseY);
    if (parent.melting != null) {
      parent.melting.drawHeatTooltips(graphics, mouseX, mouseY);
    }
  }
}
