package modernmods.modernfoundry.tables.client.inventory.module;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import modernmods.mantle.client.screen.MultiModuleScreen;
import modernmods.mantle.inventory.BaseContainerMenu;
import modernmods.modernfoundry.tables.block.entity.chest.AbstractChestBlockEntity;
import modernmods.modernfoundry.tables.block.entity.inventory.IChestItemHandler;
import modernmods.modernfoundry.tables.block.entity.inventory.IScalingContainer;

public class ScalingChestScreen<T extends BlockEntity> extends DynamicContainerScreen<MultiModuleScreen<?>,BaseContainerMenu<T>> {
  private final IScalingContainer scaling;
  public ScalingChestScreen(MultiModuleScreen<?> parent, BaseContainerMenu<T> container, Inventory playerInventory, Component title) {
    super(parent, container, playerInventory, title);
    BlockEntity tile = container.getTile();
    // the chest's own item handler is an IScalingContainer, so read it directly off the block entity to preserve the visual size logic
    if (tile instanceof AbstractChestBlockEntity chest) {
      IChestItemHandler handler = chest.getItemHandler();
      this.scaling = handler;
    } else {
      this.scaling = () -> 0;
    }
    this.slotCount = scaling.getVisualSize();
    this.sliderActive = true;
  }

  @Override
  public void updatePosition(int parentX, int parentY, int parentSizeX, int parentSizeY) {
    this.leftPos = parentX + this.xOffset;
    this.topPos = parentY + this.yOffset;

    // calculate rows and columns from space
    this.columns = (this.imageWidth - this.slider.width) / slot.w;
    this.rows = this.imageHeight / slot.h;

    this.updateSlider();
    this.updateSlots();
  }

  @Override
  protected void updateSlider() {
    this.sliderActive = this.slotCount > this.columns * this.rows;
    super.updateSlider();
    this.slider.setEnabled(this.sliderActive);
    this.slider.show();
  }

  @Override
  public void update(int mouseX, int mouseY) {
    this.slotCount = this.scaling.getVisualSize();
    super.update(mouseX, mouseY);
    this.updateSlider();
    this.updateSlots();
  }

  @Override
  public boolean shouldDrawSlot(Slot slot) {
    if (slot.getSlotIndex() >= this.scaling.getVisualSize()) {
      return false;
    }
    return super.shouldDrawSlot(slot);
  }

  @Override
  public void handleDrawGuiContainerForegroundLayer(GuiGraphicsExtractor graphics, int x, int y) {}
}
