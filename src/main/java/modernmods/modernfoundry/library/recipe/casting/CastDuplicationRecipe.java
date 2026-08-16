package modernmods.modernfoundry.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.hilt.data.loadable.common.IngredientLoadable;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.IMultiRecipe;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.recipe.helper.LoadableRecipeSerializer;
import modernmods.hilt.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.hilt.recipe.ingredient.FluidIngredient;

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

  public CastDuplicationRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, FluidIngredient fluid, int coolingTime) {
    super(serializer, id, group, cast, fluid, ItemOutput.EMPTY, coolingTime, false, false);
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    return inv.getStack().copy();
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    ItemStack[] items = getCast().getItems();
    return items.length == 0 ? ItemStack.EMPTY : items[0];
  }

  /* JEI */
  private List<DisplayCastingRecipe> displayRecipes = null;

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      displayRecipes = Arrays.stream(getCast().getItems())
        .map(item -> new DisplayCastingRecipe(getId(), getType(), List.of(item), fluid.getFluids(), item, coolingTime, false))
        .toList();
    }
    return displayRecipes;
  }
}
