package modernmods.modernfoundry.tools.recipe;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.mantle.data.loadable.Loadables;
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
    save(consumer, Loadables.ITEM.getKey(tools.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new)[0].getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new ToggleInteractionWorktableRecipe(id, tools, inputs), ToggleInteractionWorktableRecipe.LOADER, advancementId));
  }
}
