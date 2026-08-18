package modernmods.modernfoundry.tools.recipe;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.library.recipe.worktable.AbstractSizedIngredientRecipeBuilder;

import java.util.function.Consumer;

/** Builder for modifier sorting recipes */
@RequiredArgsConstructor(staticName = "sorting")
public class ModifierSortingRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ModifierSortingRecipeBuilder> {

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey((inputs.get(0).getMatchingStacks().get(0).getItem())));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new ModifierSortingRecipe(id, inputs), ModifierSortingRecipe.LOADER, advancementId));
  }
}
