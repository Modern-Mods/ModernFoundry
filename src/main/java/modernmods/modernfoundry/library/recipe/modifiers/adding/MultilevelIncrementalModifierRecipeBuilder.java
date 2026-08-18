package modernmods.modernfoundry.library.recipe.modifiers.adding;

import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.modernfoundry.library.modifiers.ModifierId;

import java.util.function.Consumer;

/** Builder for {@link MultilevelIncrementalModifierRecipe} */
public class MultilevelIncrementalModifierRecipeBuilder extends AbstractMultilevelModifierRecipeBuilder<MultilevelIncrementalModifierRecipeBuilder> {
  @javax.annotation.Nullable private Ingredient input = null;
  private int amountPerItem;
  private int neededPerLevel;
  private ItemOutput leftover = ItemOutput.EMPTY;

  protected MultilevelIncrementalModifierRecipeBuilder(ModifierId result) {
    super(result);
  }

  /** Creates a new builder instance */
  public static MultilevelIncrementalModifierRecipeBuilder modifier(ModifierId result) {
    return new MultilevelIncrementalModifierRecipeBuilder(result);
  }


  /* Inputs */

  /**
   * Adds an input to the recipe
   * @param input          Input
   * @param amountPerItem  Amount each item matches
   * @param neededPerLevel Total number needed for this modifier
   * @return  Builder instance
   */
  public MultilevelIncrementalModifierRecipeBuilder setInput(Ingredient input, int amountPerItem, int neededPerLevel) {
    if (amountPerItem < 1) {
      throw new IllegalArgumentException("Amount per item must be at least 1");
    }
    if (neededPerLevel <= amountPerItem) {
      throw new IllegalArgumentException("Needed per level must be greater than amount per item");
    }
    this.input = input;
    this.amountPerItem = amountPerItem;
    this.neededPerLevel = neededPerLevel;
    return this;
  }

  /**
   * Adds an input to the recipe
   * @param item           Item input
   * @param amountPerItem  Amount each item matches
   * @param neededPerLevel Total number needed for this modifier
   * @return  Builder instance
   */
  public MultilevelIncrementalModifierRecipeBuilder setInput(ItemLike item, int amountPerItem, int neededPerLevel) {
    return setInput(Ingredient.of(item), amountPerItem, neededPerLevel);
  }

  /**
   * Adds an input to the recipe
   * @param tag            Tag input
   * @param amountPerItem  Amount each item matches
   * @param neededPerLevel Total number needed for this modifier
   * @return  Builder instance
   */
  public MultilevelIncrementalModifierRecipeBuilder setInput(TagKey<Item> tag, int amountPerItem, int neededPerLevel) {
    return setInput(modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(tag), amountPerItem, neededPerLevel);
  }


  /* Leftover */

  /** Sets the leftover to the given output */
  public MultilevelIncrementalModifierRecipeBuilder setLeftover(ItemOutput leftover) {
    this.leftover = leftover;
    return this;
  }

  /** Sets the leftover to the given stack */
  public MultilevelIncrementalModifierRecipeBuilder setLeftover(ItemStack stack) {
    return setLeftover(ItemOutput.fromStack(stack));
  }

  /** Sets the leftover to the given item */
  public MultilevelIncrementalModifierRecipeBuilder setLeftover(ItemLike item) {
    return setLeftover(ItemOutput.fromItem(item));
  }


  /* Saving */

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (input == null) {
      throw new IllegalStateException("Must set input");
    }
    if (levels.isEmpty()) {
      throw new IllegalStateException("Must have at least 1 level");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new MultilevelIncrementalModifierRecipe(id, input, amountPerItem, neededPerLevel, tools, maxToolSize, result, leftover, allowCrystal, levels, checkTraitLevel), MultilevelIncrementalModifierRecipe.LOADER, advancementId));
  }
}
