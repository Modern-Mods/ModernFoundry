package modernmods.modernfoundry.library.client.book.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import modernmods.mantle.client.book.data.BookData;
import modernmods.mantle.client.screen.book.ArrowButton;
import modernmods.mantle.client.screen.book.element.ArrowElement;
import modernmods.mantle.client.screen.book.element.BookElement;
import modernmods.modernfoundry.library.client.book.content.ContentModifier;

import java.util.ArrayList;
import java.util.Collections;

public class CycleRecipeElement extends ArrowElement {

  public CycleRecipeElement(int x, int y, ArrowButton.ArrowType arrowType, int arrowColor, int arrowColorHover, ContentModifier modifier, BookData book, ArrayList<BookElement> list) {
    super(x, y, arrowType, arrowColor, arrowColorHover, (button) -> modifier.nextRecipe(book, list));
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (this.button != null && this.isHovered(mouseX, mouseY)) {
      this.playDownSound(Minecraft.getInstance().getSoundManager());
      // 26.1: Button.onPress now takes an InputWithModifiers; the recipe-cycle handler ignores it, so a synthetic input is fine
      this.button.onPress(new net.minecraft.client.input.InputWithModifiers() {
        @Override public int input() { return 0; }
        @Override public int modifiers() { return 0; }
      });
    }
  }

  public void playDownSound(SoundManager handler) {
    handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
  }

  @Override
  public void drawOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    if (this.isHovered(mouseX, mouseY)) {
      this.drawTooltip(graphics, Collections.singletonList(Component.translatable("gui.modernfoundry.manual.cycle.recipes")), mouseX, mouseY, fontRenderer);
    }
  }
}
