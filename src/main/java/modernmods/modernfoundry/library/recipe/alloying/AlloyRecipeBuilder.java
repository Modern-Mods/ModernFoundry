package modernmods.modernfoundry.library.recipe.alloying;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.mantle.recipe.helper.FluidOutput;
import modernmods.mantle.recipe.ingredient.FluidIngredient;
import modernmods.mantle.registration.object.FluidObject;
import modernmods.modernfoundry.library.recipe.alloying.AlloyRecipe.AlloyIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static modernmods.modernfoundry.library.recipe.melting.IMeltingRecipe.getTemperature;

/** Builder for alloy recipes */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "alloy")
public class AlloyRecipeBuilder extends AbstractRecipeBuilder<AlloyRecipeBuilder> {
  private final FluidOutput output;
  private final int temperature;
  private final List<AlloyIngredient> inputs = new ArrayList<>();

  /**
   * Creates a new recipe producing the given fluid
   * @param fluid    fluid to alloy
   * @param amount   fluid amount
   * @return  Builder instance
   */
  public static AlloyRecipeBuilder alloy(FluidObject<?> fluid, int amount) {
    return alloy(fluid.result(amount), getTemperature(fluid));
  }

  /**
   * Creates a new recipe producing the given fluid
   * @param fluid   Fluid output
   * @return  Builder instance
   */
  public static AlloyRecipeBuilder alloy(FluidStack fluid) {
    return alloy(FluidOutput.fromStack(fluid), getTemperature(fluid));
  }

  /**
   * Creates a new recipe producing the given fluid
   * @param fluid   Fluid output
   * @param amount  Output amount
   * @return  Builder instance
   */
  public static AlloyRecipeBuilder alloy(Fluid fluid, int amount) {
    // build a FluidOutput directly, deferring FluidStack construction (components not bound at datagen)
    return alloy(FluidOutput.fromFluid(fluid, amount), getTemperature(fluid));
  }


  /* Inputs */

  /**
   * Adds an input
   * @param input  Input ingredient
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(FluidIngredient input) {
    inputs.add(new AlloyIngredient(input, false));
    return this;
  }

  /**
   * Adds an input that is not consumed
   * @param input  Catalyst ingredient
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addCatalyst(FluidIngredient input) {
    inputs.add(new AlloyIngredient(input, true));
    return this;
  }

  /**
   * Adds an input
   * @param input  Input fluid
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(FluidStack input) {
    return addInput(FluidIngredient.of(input));
  }

  /**
   * Adds an input
   * @param fluid   Input fluid
   * @param amount  Input amount
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(Fluid fluid, int amount) {
    // FluidIngredient.of(fluid, amount) defers FluidStack construction (components not bound at datagen)
    return addInput(FluidIngredient.of(fluid, amount));
  }

  /**
   * Adds an input
   * @param tag     Input tag
   * @param amount  Input amount
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(TagKey<Fluid> tag, int amount) {
    return addInput(FluidIngredient.of(tag, amount));
  }


  /* Building */

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.FLUID.getKey(output.get().getFluid()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (inputs.size() < 2) {
      throw new IllegalStateException("Invalid alloying recipe " + id + ", must have at least two inputs");
    }
    consumer.accept(new LoadableFinishedRecipe<>(id,
      new AlloyRecipe(id, inputs, output, temperature),
      AlloyRecipe.LOADER,
      this.buildOptionalAdvancement(id, "alloys")
    ));
  }
}
