package modernmods.modernfoundry.library.recipe.molding;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import modernmods.hilt.data.loadable.common.IngredientLoadable;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.primitive.BooleanLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.ICommonRecipe;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.recipe.helper.LoadableRecipeSerializer;
import modernmods.hilt.recipe.helper.TypeAwareRecipeSerializer;

/** Recipe to combine two items on the top of a casting table, changing the first */
public class MoldingRecipe implements ICommonRecipe<IMoldingContainer> {
  public static final RecordLoadable<MoldingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(),
    ContextKey.ID.requiredField(),
    IngredientLoadable.DISALLOW_EMPTY.requiredField("material", MoldingRecipe::getMaterial),
    IngredientLoadable.ALLOW_EMPTY.defaultField("pattern", Ingredient.EMPTY, MoldingRecipe::getPattern),
    BooleanLoadable.INSTANCE.defaultField("pattern_consumed", false, false, MoldingRecipe::isPatternConsumed),
    ItemOutput.Loadable.REQUIRED_ITEM.requiredField("result", r -> r.recipeOutput),
    MoldingRecipe::new);

  @Getter
  private final RecipeType<?> type;
  @Getter
  private final RecipeSerializer<?> serializer;
  @Getter
  private final ResourceLocation id;
  @Getter
  private final Ingredient material;
  @Getter
  private final Ingredient pattern;
  @Getter
  private final boolean patternConsumed;
  private final ItemOutput recipeOutput;

  public MoldingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, Ingredient material, Ingredient pattern, boolean patternConsumed, ItemOutput recipeOutput) {
    this.type = serializer.getType();
    this.serializer = serializer;
    this.id = id;
    this.material = material;
    this.pattern = pattern;
    this.patternConsumed = pattern != Ingredient.EMPTY && patternConsumed;
    this.recipeOutput = recipeOutput;
  }

  @Override
  public boolean matches(IMoldingContainer inv, Level worldIn) {
    return material.test(inv.getMaterial()) && pattern.test(inv.getPattern());
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(Ingredient.EMPTY, material, pattern);
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return recipeOutput.get();
  }
}
