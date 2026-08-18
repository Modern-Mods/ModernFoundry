package modernmods.modernfoundry.plugin.jei.util;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** @deprecated use {@link FluidTooltipCallback} for better handling of advanced tooltip information */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface IRecipeTooltipReplacement extends IRecipeSlotRichTooltipCallback {
  /** Tooltip replacement that keeps just the name and mod ID */
  IRecipeTooltipReplacement EMPTY = (slot, tooltip) -> {};

  @Override
  default void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder builder) {
    List<Component> tooltip = FluidTooltipCallback.extractText(builder);
    List<Component> result = new ArrayList<>();
    if (!tooltip.isEmpty()) {
      result.add(tooltip.get(0));
    }
    addMiddleLines(recipeSlotView, result);
    FluidTooltipCallback.applyText(builder, result);
  }

  /** Adds the lines between the name and mod ID */
  void addMiddleLines(IRecipeSlotView recipeSlotView, List<Component> tooltip);
}
