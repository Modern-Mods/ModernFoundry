package modernmods.modernfoundry.plugin.jei.util;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.fluid.tooltip.FluidTooltipHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/** Helper for working with fluid tooltips */
@FunctionalInterface
public interface FluidTooltipCallback extends IRecipeSlotRichTooltipCallback {
  String AMOUNT_KEY = "jei.tooltip.liquid.amount";

  /** Default instance, simply replaces mb units with our unit handler. */
  FluidTooltipCallback UNITS = (fluid, recipeSlotView, tooltip) -> FluidTooltipHandler.appendMaterial(fluid, tooltip);

  /** Default instance, simply replaces mb units with our unit handler. */
  FluidTooltipCallback NO_AMOUNT = (fluid, recipeSlotView, tooltip) -> {};

  /** Reads the plain text lines currently present in the tooltip builder */
  static List<Component> extractText(ITooltipBuilder builder) {
    List<Component> list = new ArrayList<>();
    for (Either<FormattedText, TooltipComponent> line : builder.getLines()) {
      line.left().ifPresent(text -> {
        if (text instanceof Component component) {
          list.add(component);
        }
      });
    }
    return list;
  }

  /** Replaces the plain text lines in the builder with the given list, keeping any ingredient */
  static void applyText(ITooltipBuilder builder, List<Component> lines) {
    // only clear text lines; builder.clear() would also drop the slot ingredient
    builder.getLines().clear();
    lines.forEach(builder::add);
  }

  @Override
  default void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder builder) {
    List<Component> tooltip = extractText(builder);
    ListIterator<Component> listIterator = tooltip.listIterator();
    while (listIterator.hasNext()) {
      Component component = listIterator.next();
      if (component.getContents() instanceof TranslatableContents translatable && AMOUNT_KEY.equals(translatable.getKey())) {
        listIterator.remove();
        FluidStack fluid = recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
        List<Component> newTooltip = new ArrayList<>();
        onFluidTooltip(fluid, recipeSlotView, newTooltip);
        tooltip.addAll(listIterator.nextIndex(), newTooltip);
        applyText(builder, tooltip);
        return;
      }
    }
    // failed to find the tooltip to replace, so just append our stuff at the end
    FluidStack fluid = recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
    onFluidTooltip(fluid, recipeSlotView, tooltip);
    applyText(builder, tooltip);
  }

  /** Adds information about the fluid to the tooltip */
  void onFluidTooltip(FluidStack fluid, IRecipeSlotView recipeSlotView, List<Component> tooltip);
}
