package modernmods.modernfoundry.library.recipe.molding;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.primitive.BooleanLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.ICommonRecipe;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;

/** Recipe to combine two items on the top of a casting table, changing the first */
public class MoldingRecipe implements ICommonRecipe<IMoldingContainer> {
  public static final RecordLoadable<MoldingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(),
    ContextKey.ID.requiredField(),
    IngredientLoadable.DISALLOW_EMPTY.requiredField("material", MoldingRecipe::getMaterial),
    IngredientLoadable.ALLOW_EMPTY.nullableField("pattern", MoldingRecipe::getPattern),
    BooleanLoadable.INSTANCE.defaultField("pattern_consumed", false, false, MoldingRecipe::isPatternConsumed),
    ItemOutput.Loadable.REQUIRED_ITEM.requiredField("result", r -> r.recipeOutput),
    MoldingRecipe::new);

  @Getter
  private final RecipeType<? extends MoldingRecipe> type;
  @Getter(lombok.AccessLevel.NONE)
  private final TypeAwareRecipeSerializer<?> serializer;
  @Getter
  private final Identifier id;
  @Getter
  private final Ingredient material;
  @Getter @javax.annotation.Nullable
  private final Ingredient pattern;
  @Getter
  private final boolean patternConsumed;
  private final ItemOutput recipeOutput;

  @SuppressWarnings("unchecked")
  public MoldingRecipe(TypeAwareRecipeSerializer<?> serializer, Identifier id, Ingredient material, @javax.annotation.Nullable Ingredient pattern, boolean patternConsumed, ItemOutput recipeOutput) {
    this.type = (RecipeType<? extends MoldingRecipe>) serializer.getType();
    this.serializer = serializer;
    this.id = id;
    this.material = material;
    this.pattern = pattern;
    this.patternConsumed = pattern != null && patternConsumed;
    this.recipeOutput = recipeOutput;
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeSerializer<? extends MoldingRecipe> getSerializer() {
    return (RecipeSerializer<? extends MoldingRecipe>) serializer.serializer();
  }

  @Override
  public boolean matches(IMoldingContainer inv, Level worldIn) {
    return material.test(inv.getMaterial()) && (pattern == null ? inv.getPattern().isEmpty() : pattern.test(inv.getPattern()));
  }

  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    list.add(material);
    if (pattern != null) {
      list.add(pattern);
    }
    return list;
  }

  public ItemStack getResultItem(HolderLookup.Provider access) {
    return recipeOutput.get();
  }

  @Override
  public ItemStack assemble(IMoldingContainer inv) {
    // Mantle's ICommonRecipe#assemble defaults to empty; molding must return the actual output so the table produces the item
    return recipeOutput.get().copy();
  }
}
