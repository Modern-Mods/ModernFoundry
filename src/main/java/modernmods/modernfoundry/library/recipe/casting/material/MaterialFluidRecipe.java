package modernmods.modernfoundry.library.recipe.casting.material;

import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.ICustomOutputRecipe;
import modernmods.mantle.recipe.ingredient.FluidIngredient;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariant;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;
import modernmods.modernfoundry.library.recipe.casting.ICastingContainer;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.List;

/** Recipe defining casting and composite fluids for a given input */
public class MaterialFluidRecipe implements ICustomOutputRecipe<ICastingContainer> {
  public static final RecordLoadable<MaterialFluidRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    FluidIngredient.LOADABLE.requiredField("fluid", r -> r.fluid),
    IntLoadable.FROM_ZERO.requiredField("temperature", r -> r.temperature),
    MaterialVariantId.LOADABLE.nullableField("input", r -> r.input != null ? r.input.getVariant() : null),
    MaterialVariantId.LOADABLE.nullableField("output", r -> r.output.getVariant()),
    MaterialFluidRecipe::new);
  /** Empty recipe instance, used as a fallback */
  public static final MaterialFluidRecipe EMPTY = new MaterialFluidRecipe(TConstruct.getResource("missingno"), FluidIngredient.EMPTY, 0, null, IMaterial.UNKNOWN_ID);

  @Getter
  private final Identifier id;
  private final FluidIngredient fluid;
  @Getter
  private final int temperature;
  /** Material base for composite */
  @Nullable @Getter
  private final MaterialVariant input;
  /** Output material ID */
  @Getter
  private final MaterialVariant output;

  public MaterialFluidRecipe(Identifier id, FluidIngredient fluid, int temperature, @Nullable MaterialVariantId inputId, MaterialVariantId outputId) {
    this.id = id;
    this.fluid = fluid;
    this.temperature = temperature;
    this.input = inputId == null ? null : MaterialVariant.of(inputId);
    this.output = MaterialVariant.of(outputId);
    MaterialCastingLookup.registerFluid(this);
  }

  /** Checks if this recipe is valid for the given fluid and material */
  public boolean matches(Fluid fluid) {
    return !output.isUnknown() && this.fluid.test(fluid);
  }

  /** Checks if this recipe is valid for the given fluid and material */
  public boolean matches(Fluid fluid, MaterialVariantId material) {
    // disallow casting if the input material matches the output (including variant) to prevent wasted resources
    return matches(fluid) && (input == null || input.matchesVariant(material)) && !output.sameVariant(material);
  }

  /** Gets the amount of fluid to cast this recipe */
  public int getFluidAmount(Fluid fluid) {
    return this.fluid.getAmount(fluid);
  }

  /** Gets a list of fluids for display */
  public List<FluidStack> getFluids() {
    return fluid.getFluids();
  }

  @Override
  public final boolean matches(ICastingContainer inv, Level worldIn) {
    // if the input ID is null, can skip fetching the input stack material
    return matches(inv.getFluid()) && (input == null || input.matchesVariant(inv.getStack()));
  }

  @Override
  public RecipeSerializer<? extends MaterialFluidRecipe> getSerializer() {
    return TinkerSmeltery.materialFluidRecipe.get();
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeType<? extends MaterialFluidRecipe> getType() {
    return (RecipeType<? extends MaterialFluidRecipe>)(RecipeType<?>) TinkerRecipeTypes.DATA.get();
  }

  /** Checks that all materials in this recipe are known */
  public boolean isVisible() {
    return !output.isUnknown() && !output.get().isHidden()
      && (input == null || !input.isUnknown() && !input.get().isHidden());
  }
}
