package modernmods.modernfoundry.tables.client.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import modernmods.modernfoundry.tables.block.entity.table.CraftingStationBlockEntity;
import modernmods.modernfoundry.tables.menu.CraftingStationContainerMenu;

public class CraftingStationScreen extends BaseTabbedScreen<CraftingStationBlockEntity,CraftingStationContainerMenu> {
  private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = ResourceLocation.parse("textures/gui/container/crafting_table.png");

  public CraftingStationScreen(CraftingStationContainerMenu container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);
    addChestSideInventory(playerInventory);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
    this.drawBackground(graphics, CRAFTING_TABLE_GUI_TEXTURES);
    super.renderBg(graphics, partialTicks, mouseX, mouseY);
  }
}
