package modernmods.modernfoundry.tables.client.inventory;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import modernmods.modernfoundry.tables.block.entity.chest.AbstractChestBlockEntity;
import modernmods.modernfoundry.tables.client.inventory.module.ScalingChestScreen;
import modernmods.modernfoundry.tables.menu.TabbedContainerMenu;
import modernmods.modernfoundry.tables.menu.TinkerChestContainerMenu;

public class TinkerChestScreen extends BaseTabbedScreen<AbstractChestBlockEntity,TabbedContainerMenu<AbstractChestBlockEntity>> {
  // TODO: can this safely be removed? its unused
  //protected static final ScalableElementScreen BACKGROUND = new ScalableElementScreen(7 + 18, 7, 18, 18);
  public ScalingChestScreen<AbstractChestBlockEntity> scalingChestScreen;

  public TinkerChestScreen(TabbedContainerMenu<AbstractChestBlockEntity> container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);

    this.imageHeight = 184;
    TinkerChestContainerMenu.DynamicChestInventory chestContainer = container.getSubContainer(TinkerChestContainerMenu.DynamicChestInventory.class);
    if (chestContainer != null) {
      this.scalingChestScreen = new ScalingChestScreen<>(this, chestContainer, playerInventory, title);
      // add one extra row to the height
      this.scalingChestScreen.adjustBounds(0, 0, 0, 18);
      this.addModule(scalingChestScreen);
    }
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    this.drawBackground(graphics, BLANK_BACK_PLUS_1);

    if (this.scalingChestScreen != null) {
      this.scalingChestScreen.update(mouseX, mouseY);
    }

    super.extractBackground(graphics, mouseX, mouseY, partialTicks);
  }

  @Override
  public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
    double mouseX = event.x(); double mouseY = event.y(); int mouseButton = event.button();
    if (this.scalingChestScreen == null) {
      return false;
    }

    if (this.scalingChestScreen.handleMouseClicked(mouseX, mouseY, mouseButton)) {
      return false;
    }

    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
    double mouseX = event.x(); double mouseY = event.y(); int button = event.button();
    if (this.scalingChestScreen == null) {
      return false;
    }

    if (this.scalingChestScreen.handleMouseClickMove(mouseX, mouseY, button, dragX)) {
      return false;
    }

    return super.mouseDragged(event, dragX, dragY);
  }

  @Override
  // NOTE(26.1 port): this 3-arg mouseScrolled overrides Mantle's MultiModuleScreen helper, not vanilla's 4-arg entry point.
  // Validate chest side-panel mouse-wheel scroll in-game (may need routing via the 4-arg mouseScrolled override).
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (this.scalingChestScreen == null) {
      return false;
    }

    if (this.scalingChestScreen.handleMouseScrolled(mouseX, mouseY, delta)) {
      return false;
    }

    return super.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
    double mouseX = event.x(); double mouseY = event.y(); int state = event.button();
    if (this.scalingChestScreen == null) {
      return false;
    }

    if (this.scalingChestScreen.handleMouseReleased(mouseX, mouseY, state)) {
      return false;
    }

    return super.mouseReleased(event);
  }
}
