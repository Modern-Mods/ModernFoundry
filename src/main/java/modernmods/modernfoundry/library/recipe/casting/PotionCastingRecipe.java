package modernmods.modernfoundry.library.recipe.casting;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.compat.neoforged.neoforge.registries.ForgeRegistries;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.IMultiRecipe;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.mantle.recipe.ingredient.FluidIngredient;
import modernmods.modernfoundry.library.utils.TagUtil;

import java.util.List;

/**
 * Recipe for casting a fluid onto an item, copying the fluid NBT to the item
 */
public class PotionCastingRecipe implements ICastingRecipe, IMultiRecipe<DisplayCastingRecipe> {
  protected static final LoadableField<FluidIngredient, PotionCastingRecipe> FLUID_FIELD = FluidIngredient.LOADABLE.requiredField("fluid", r -> r.fluid);
  protected static final LoadableField<Integer, PotionCastingRecipe> COOLING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooling_time", 5, r -> r.coolingTime);
  public static final RecordLoadable<PotionCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("bottle", r -> r.bottle),
    FLUID_FIELD,
    Loadables.ITEM.requiredField("result", r -> r.result),
    COOLING_TIME_FIELD,
    PotionCastingRecipe::new);

  @Getter(lombok.AccessLevel.NONE)
  protected final TypeAwareRecipeSerializer<?> serializer;
  @Getter
  protected final Identifier id;
  @Getter
  protected final String group;
  /** Input on the casting table, always consumed */
  protected final Ingredient bottle;
  /** Potion ingredient, typically just the potion tag */
  protected final FluidIngredient fluid;
  /** Potion item result, will be given the proper NBT */
  protected final Item result;
  /** Cooling time for this recipe, used for tipped arrows */
  protected final int coolingTime;

  public PotionCastingRecipe(TypeAwareRecipeSerializer<?> serializer, Identifier id, String group, Ingredient bottle, FluidIngredient fluid, Item result, int coolingTime) {
    this.serializer = serializer;
    this.id = id;
    this.group = group;
    this.bottle = bottle;
    this.fluid = fluid;
    this.result = result;
    this.coolingTime = coolingTime;
    CastingRecipeLookup.registerCastable(result);
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeType<? extends PotionCastingRecipe> getType() {
    return (RecipeType<? extends PotionCastingRecipe>) serializer.getType();
  }

  @Override
  @SuppressWarnings("unchecked")
  public net.minecraft.world.item.crafting.RecipeSerializer<? extends PotionCastingRecipe> getSerializer() {
    return (net.minecraft.world.item.crafting.RecipeSerializer<? extends PotionCastingRecipe>) serializer.serializer();
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    return bottle.test(inv.getStack()) && fluid.test(inv.getFluid());
  }

  @Override
  public int getFluidAmount(ICastingContainer inv) {
    return fluid.getAmount(inv.getFluid());
  }

  @Override
  public boolean isConsumed() {
    return true;
  }

  @Override
  public boolean switchSlots() {
    return false;
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    return coolingTime;
  }

  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = new ItemStack(this.result);
    PotionUtils.setPotion(result, PotionUtils.getPotion(inv.getFluidTag()));
    return result;
  }


  /* JEI */
  protected List<DisplayCastingRecipe> displayRecipes = null;

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      // create a subrecipe for every potion variant
      List<ItemStack> bottles = List.of(bottle.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new));
      displayRecipes = ForgeRegistries.POTIONS.getValues().stream()
        .filter(potion -> potion != Potions.WATER.value())
        .map(potion -> {
          ItemStack result = PotionUtils.setPotion(new ItemStack(this.result), potion);
          return new DisplayCastingRecipe(getId(), getType(), bottles, fluid.getFluids().stream()
                                                              .map(fluid -> TagUtil.createFluidStack(fluid.getFluid(), fluid.getAmount(), TagUtil.getTag(result)))
                                                              .toList(),
                                          result, coolingTime, true);
        }).toList();
    }
    return displayRecipes;
  }


  /* Recipe interface methods */

  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    list.add(bottle);
    return list;
  }

  /** @deprecated use {@link #assemble(Container, HolderLookup.Provider)} */
  @Deprecated
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return new ItemStack(this.result);
  }
}
