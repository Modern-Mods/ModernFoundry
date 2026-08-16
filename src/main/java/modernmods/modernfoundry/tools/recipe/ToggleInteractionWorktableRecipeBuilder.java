package modernmods.modernfoundry.tools.recipe;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import modernmods.hilt.recipe.data.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.modernfoundry.library.recipe.worktable.AbstractSizedIngredientRecipeBuilder;
import modernmods.modernfoundry.library.recipe.worktable.AbstractWorktableRecipe;

import java.util.function.Consumer;

/** Builder for {@link ToggleInteractionWorktableRecipe} */
@Accessors(fluent = true)
@Setter
@NoArgsConstructor(staticName = "builder")
public class ToggleInteractionWorktableRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ToggleInteractionWorktableRecipeBuilder> {
  private Ingredient tools = AbstractWorktableRecipe.DEFAULT_TOOLS;

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Loadables.ITEM.getKey(tools.getItems()[0].getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    ResourceLocation advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new ToggleInteractionWorktableRecipe(id, tools, inputs), ToggleInteractionWorktableRecipe.LOADER, advancementId));
  }
}
