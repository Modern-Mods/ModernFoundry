package modernmods.modernfoundry.tables.client.inventory;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import modernmods.modernfoundry.tables.block.entity.table.CraftingStationBlockEntity;
import modernmods.modernfoundry.tables.menu.CraftingStationContainerMenu;

public class CraftingStationScreen extends BaseTabbedScreen<CraftingStationBlockEntity,CraftingStationContainerMenu> {
  private static final Identifier CRAFTING_TABLE_GUI_TEXTURES = Identifier.parse("textures/gui/container/crafting_table.png");

  public CraftingStationScreen(CraftingStationContainerMenu container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);
    addChestSideInventory(playerInventory);
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    this.drawBackground(graphics, CRAFTING_TABLE_GUI_TEXTURES);
    super.extractBackground(graphics, mouseX, mouseY, partialTicks);
  }
}
