package modernmods.modernfoundry.library.recipe.casting;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.mantle.recipe.ingredient.FluidIngredient;

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

  @Getter(lombok.AccessLevel.NONE)
  private final TypeAwareRecipeSerializer<?> serializer;
  protected final FluidIngredient fluid;
  protected final ItemOutput result;
  protected final int coolingTime;
  public ItemCastingRecipe(TypeAwareRecipeSerializer<?> serializer, Identifier id, String group, Ingredient cast, FluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(serializer.getType(), id, group, cast, consumed, switchSlots);
    this.serializer = serializer;
    this.fluid = fluid;
    this.result = result;
    this.coolingTime = coolingTime;
    CastingRecipeLookup.registerCastable(result);
  }

  @Override
  @SuppressWarnings("unchecked")
  public net.minecraft.world.item.crafting.RecipeSerializer<? extends ItemCastingRecipe> getSerializer() {
    return (net.minecraft.world.item.crafting.RecipeSerializer<? extends ItemCastingRecipe>) serializer.serializer();
  }

  @Override
  public int getFluidAmount(ICastingContainer inv) {
    return this.fluid.getAmount(inv.getFluid());
  }

  @Override
  public boolean matches(ICastingContainer inv, Level worldIn) {
    // testCast handles the nullable cast (null = no cast -> empty slot); getCast().test NPEs on a no-cast recipe
    return testCast(inv.getStack()) && fluid.test(inv.getFluid());
  }

  public ItemStack getResultItem(HolderLookup.Provider access) {
    return this.result.get();
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    // copy so the cooled output placed into the table slot never aliases the recipe's cached result stack
    return this.result.get().copy();
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    return this.coolingTime;
  }


  /* JEI */

  @Override
  public Identifier getRecipeId() {
    // need a separate method as remapping makes the names mismatch
    return getId();
  }

  @Override
  public boolean hasCast() {
    return getCast() != null;
  }

  @Override
  public List<ItemStack> getCastItems() {
    // cast is nullable (null = no cast; the item is poured into an empty basin/table). getCast().items() NPEs on those,
    // and setRecipe calls this before checking isEmpty(), so a no-cast recipe (metal blocks, glass, ...) would throw and
    // JEI would drop it -> the recipe wouldn't appear when looking it up. Return no cast items instead.
    net.minecraft.world.item.crafting.Ingredient cast = getCast();
    if (cast == null) {
      return List.of();
    }
    return Arrays.asList(cast.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new));
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
