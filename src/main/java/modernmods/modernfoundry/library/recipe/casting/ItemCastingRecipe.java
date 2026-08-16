package modernmods.modernfoundry.library.recipe.casting;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.field.LoadableField;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.recipe.helper.LoadableRecipeSerializer;
import modernmods.hilt.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.hilt.recipe.ingredient.FluidIngredient;

import java.util.Arrays;
import java.util.List;

/** Casting recipe that takes a fluid and optional cast and outputs an item. */
@Getter
public class ItemCastingRecipe extends AbstractCastingRecipe implements IDisplayableCastingRecipe {
  /* Shared fields */
  protected static final LoadableField<FluidIngredient,ItemCastingRecipe> FLUID_FIELD = FluidIngredient.LOADABLE.requiredField("fluid", ItemCastingRecipe::getFluid);
  protected static final LoadableField<ItemOutput,ItemCastingRecipe> RESULT_FIELD = ItemOutput.Loadable.REQUIRED_ITEM.requiredField("result", r -> r.result);
  protected static final LoadableField<Integer,ItemCastingRecipe> COOLING_TIME_FIELD = IntLoadable.FROM_ONE.requiredField("cooling_time", ItemCastingRecipe::getCoolingTime);
  /** Loader instance */
  public static final RecordLoadable<ItemCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP, CAST_FIELD, FLUID_FIELD, RESULT_FIELD, COOLING_TIME_FIELD, CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    ItemCastingRecipe::new);

  private final TypeAwareRecipeSerializer<?> serializer;
  protected final FluidIngredient fluid;
  protected final ItemOutput result;
  protected final int coolingTime;
  public ItemCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, FluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(serializer.getType(), id, group, cast, consumed, switchSlots);
    this.serializer = serializer;
    this.fluid = fluid;
    this.result = result;
    this.coolingTime = coolingTime;
    CastingRecipeLookup.registerCastable(result);
  }

  @Override
  public int getFluidAmount(ICastingContainer inv) {
    return this.fluid.getAmount(inv.getFluid());
  }

  @Override
  public boolean matches(ICastingContainer inv, Level worldIn) {
    return getCast().test(inv.getStack()) && fluid.test(inv.getFluid());
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return this.result.get();
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    return this.coolingTime;
  }


  /* JEI */

  @Override
  public ResourceLocation getRecipeId() {
    // need a separate method as remapping makes the names mismatch
    return getId();
  }

  @Override
  public boolean hasCast() {
    return getCast() != Ingredient.EMPTY;
  }

  @Override
  public List<ItemStack> getCastItems() {
    return Arrays.asList(getCast().getItems());
  }

  @Override
  public ItemStack getOutput() {
    return this.result.get();
  }

  /**
   * Gets a list of valid fluid inputs for this recipe, for display in JEI
   * @return  List of fluids
   */
  @Override
  public List<FluidStack> getFluids() {
    return this.fluid.getFluids();
  }
}
