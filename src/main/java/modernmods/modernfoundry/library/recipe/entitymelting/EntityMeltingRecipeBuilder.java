package modernmods.modernfoundry.library.recipe.entitymelting;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.hilt.recipe.data.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.hilt.recipe.data.AbstractRecipeBuilder;
import modernmods.hilt.recipe.helper.FluidOutput;
import modernmods.hilt.recipe.ingredient.EntityIngredient;

import java.util.function.Consumer;

/** Builder for entity melting recipes */
@RequiredArgsConstructor(staticName = "melting")
public class EntityMeltingRecipeBuilder extends AbstractRecipeBuilder<EntityMeltingRecipeBuilder> {
  private final EntityIngredient ingredient;
  private final FluidOutput output;
  private final int damage;

  /** Creates a new builder */
  public static EntityMeltingRecipeBuilder melting(EntityIngredient ingredient, FluidStack output, int damage) {
    return melting(ingredient, FluidOutput.fromStack(output), damage);
  }

  /** Creates a new builder doing 2 damage */
  public static EntityMeltingRecipeBuilder melting(EntityIngredient ingredient, FluidOutput output) {
    return melting(ingredient, output, 2);
  }

  /** Creates a new builder doing 2 damage */
  public static EntityMeltingRecipeBuilder melting(EntityIngredient ingredient, FluidStack output) {
    return melting(ingredient, output, 2);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.FLUID.getKey(output.get().getFluid()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "entity_melting");
    consumer.accept(new LoadableFinishedRecipe<>(id, new EntityMeltingRecipe(id, ingredient, output, damage), EntityMeltingRecipe.LOADER, advancementId));
  }
}
