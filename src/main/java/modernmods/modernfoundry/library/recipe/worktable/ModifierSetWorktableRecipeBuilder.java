package modernmods.modernfoundry.library.recipe.worktable;

import net.minecraft.core.registries.BuiltInRegistries;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.json.predicate.modifier.ModifierPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierId;

import java.util.function.Consumer;

/** Builder for recipes to add or remove a modifier from a set in persistent data */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifierSetWorktableRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ModifierSetWorktableRecipeBuilder> {
  private final Identifier dataKey;
  @Setter @Accessors(fluent = true)
  private IJsonPredicate<ModifierId> modifierPredicate = ModifierPredicate.ANY;
  private final boolean addToSet;
  private Ingredient tools = AbstractWorktableRecipe.DEFAULT_TOOLS;
  private boolean allowTraits = false;

  /** Creates a new recipe for adding to a set */
  public static ModifierSetWorktableRecipeBuilder setAdding(Identifier dataKey) {
    return new ModifierSetWorktableRecipeBuilder(dataKey, true);
  }

  /** Creates a new recipe for removing from a set */
  public static ModifierSetWorktableRecipeBuilder setRemoving(Identifier dataKey) {
    return new ModifierSetWorktableRecipeBuilder(dataKey, false);
  }

  /** Sets the tool requirement for this recipe */
  public ModifierSetWorktableRecipeBuilder setTools(Ingredient ingredient) {
    this.tools = ingredient;
    return this;
  }

  /** Sets the tool requirement for this recipe */
  public ModifierSetWorktableRecipeBuilder setTools(TagKey<Item> tag) {
    return this.setTools(modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(tag));
  }

  /** Sets the recipe to allow traits */
  public ModifierSetWorktableRecipeBuilder allowTraits() {
    allowTraits = true;
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, dataKey);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    // no tools.isEmpty() check: an Ingredient can never be empty in 26.1 (its constructor forbids it), and calling
    // isEmpty() resolves a lazy tag ingredient's contents, throwing "Missing tag" at datagen time (tags not yet bound).
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new ModifierSetWorktableRecipe(id, dataKey, inputs, tools, modifierPredicate, addToSet, allowTraits), ModifierSetWorktableRecipe.LOADER, advancementId));
  }
}
