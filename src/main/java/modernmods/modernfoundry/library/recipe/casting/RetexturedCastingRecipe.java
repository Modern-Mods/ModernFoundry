package modernmods.modernfoundry.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.recipe.helper.LoadableRecipeSerializer;
import modernmods.hilt.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.hilt.recipe.ingredient.FluidIngredient;
import modernmods.hilt.util.RetexturedHelper;

/** Extension of item recipe that sets the result block to the input block */
public class RetexturedCastingRecipe extends ItemCastingRecipe {
  /** Loader instance */
  public static final RecordLoadable<RetexturedCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP, CAST_FIELD, FLUID_FIELD, RESULT_FIELD, COOLING_TIME_FIELD, CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    RetexturedCastingRecipe::new);

  public RetexturedCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, FluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(serializer, id, group, cast, fluid, result, coolingTime, consumed, switchSlots);
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = getResultItem(access).copy();
    if (inv.getStack().getItem() instanceof BlockItem blockItem ) {
      return RetexturedHelper.setTexture(result, blockItem.getBlock());
    }
    return result;
  }
}
