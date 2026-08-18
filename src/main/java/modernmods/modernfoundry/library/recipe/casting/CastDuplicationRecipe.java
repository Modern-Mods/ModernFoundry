package modernmods.modernfoundry.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.IMultiRecipe;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.mantle.recipe.ingredient.FluidIngredient;

import java.util.Arrays;
import java.util.List;

/** Recipe which duplicates the input cast using a fluid */
public class CastDuplicationRecipe extends ItemCastingRecipe implements IMultiRecipe<DisplayCastingRecipe> {
  public static final RecordLoadable<CastDuplicationRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("cast", CastDuplicationRecipe::getCast),
    FLUID_FIELD, COOLING_TIME_FIELD,
    CastDuplicationRecipe::new);

  public CastDuplicationRecipe(TypeAwareRecipeSerializer<?> serializer, Identifier id, String group, Ingredient cast, FluidIngredient fluid, int coolingTime) {
    super(serializer, id, group, cast, fluid, ItemOutput.EMPTY, coolingTime, false, false);
  }

  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    return inv.getStack().copy();
  }

  public ItemStack getResultItem(HolderLookup.Provider access) {
    ItemStack[] items = getCast().items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new);
    return items.length == 0 ? ItemStack.EMPTY : items[0];
  }

  /* JEI */
  private List<DisplayCastingRecipe> displayRecipes = null;

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      displayRecipes = Arrays.stream(getCast().items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new))
        .map(item -> new DisplayCastingRecipe(getId(), getType(), List.of(item), fluid.getFluids(), item, coolingTime, false))
        .toList();
    }
    return displayRecipes;
  }
}
