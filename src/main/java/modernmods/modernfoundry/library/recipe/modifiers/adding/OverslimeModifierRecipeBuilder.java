package modernmods.modernfoundry.library.recipe.modifiers.adding;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.modernfoundry.common.TinkerTags;

import java.util.function.Consumer;

/**
 * Builder for overslime recipes
 */
@RequiredArgsConstructor(staticName = "modifier")
public class OverslimeModifierRecipeBuilder extends AbstractRecipeBuilder<OverslimeModifierRecipeBuilder> {
  @Setter @Accessors(chain = true)
  private Ingredient tools = modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(TinkerTags.Items.DURABILITY);
  private final Ingredient ingredient;
  private final int restoreAmount;

  /** Creates a new builder for the given item */
  public static OverslimeModifierRecipeBuilder modifier(ItemLike item, int restoreAmount) {
    return modifier(Ingredient.of(item), restoreAmount);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    ItemStack[] stacks = ingredient.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new);
    if (stacks.length == 0) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    save(consumer, BuiltInRegistries.ITEM.getKey(stacks[0].getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (ingredient.isEmpty()) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new OverslimeModifierRecipe(id, tools, ingredient, restoreAmount), OverslimeModifierRecipe.LOADER, advancementId));
  }

  /** Creates a crafting table overslime repair recipe */
  public OverslimeModifierRecipeBuilder saveCrafting(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (ingredient.isEmpty()) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new OverslimeCraftingTableRecipe(id, tools, ingredient, restoreAmount), OverslimeCraftingTableRecipe.LOADER, advancementId));
    return this;
  }
}
