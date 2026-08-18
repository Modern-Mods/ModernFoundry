package modernmods.modernfoundry.library.recipe.casting.material;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.mantle.recipe.ingredient.FluidIngredient;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static modernmods.modernfoundry.library.recipe.melting.IMeltingRecipe.getTemperature;

/**
 * Builder to make parts and composites castable
 */
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "material")
public class MaterialFluidRecipeBuilder extends AbstractRecipeBuilder<MaterialFluidRecipeBuilder> {
  /** Output material ID */
  private final MaterialVariantId outputId;
  /** Fluid used for casting */
  @Setter
  private FluidIngredient fluid = FluidIngredient.EMPTY;
  /** Temperature for cooling time calculations */
  @Setter
  private int temperature = -1;
  /** Material base for composite */
  @Setter @Nullable
  private MaterialVariantId inputId;

  /**
   * Sets the fluid for this recipe, and cooling time if unset.
   * @param fluidStack  Fluid input
   * @return  Builder instance
   */
  public MaterialFluidRecipeBuilder setFluidAndTemp(FluidStack fluidStack) {
    this.fluid = FluidIngredient.of(fluidStack);
    if (this.temperature == -1) {
      this.temperature = getTemperature(fluidStack);
    }
    return this;
  }

  /**
   * Sets the fluid for this recipe, and cooling time
   * @param tagIn   Tag<Fluid> instance
   * @param amount  Fluid amount
   */
  public MaterialFluidRecipeBuilder setFluid(TagKey<Fluid> tagIn, int amount) {
    setFluid(FluidIngredient.of(tagIn, amount));
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, outputId.getId().getIdentifier());
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (this.fluid == FluidIngredient.EMPTY) {
      throw new IllegalStateException("Material fluid recipes require a fluid input");
    }
    if (this.temperature < 0) {
      throw new IllegalStateException("Temperature is too low, must be at least 0");
    }
    Identifier advancementId = this.buildOptionalAdvancement(id, "materials");
    consumer.accept(new LoadableFinishedRecipe<>(id, new MaterialFluidRecipe(id, fluid, temperature, inputId, outputId), MaterialFluidRecipe.LOADER, advancementId));
  }
}
