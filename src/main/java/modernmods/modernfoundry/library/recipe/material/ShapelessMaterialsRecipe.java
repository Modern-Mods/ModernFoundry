package modernmods.modernfoundry.library.recipe.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import modernmods.mantle.data.loadable.Loadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.recipe.helper.LoggingRecipeSerializer;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Shapeless recipe with a number of {@link modernmods.modernfoundry.library.recipe.ingredient.MaterialIngredient} and
 * {@link modernmods.modernfoundry.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 */
public class ShapelessMaterialsRecipe extends ShapelessRecipe implements MaterialsCraftingTableRecipe {
  private final Identifier id;
  /** Number of parts to match */
  @Getter
  private final int partCount;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;
  /** Ingredients of this recipe, used as the parts list */
  private final List<Ingredient> ingredients;

  public ShapelessMaterialsRecipe(Identifier id, Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
    super(commonInfo, bookInfo, result, ingredients);
    this.id = id;
    this.ingredients = ingredients;
    this.partCount = partCount;
    this.extraMaterials = extraMaterials;
  }

  /** Wraps a vanilla shapeless recipe, adding the part count and extra material information */
  public ShapelessMaterialsRecipe(Identifier id, ShapelessRecipe recipe, int partCount, List<MaterialVariantId> extraMaterials) {
    this(id, new Recipe.CommonInfo(recipe.showNotification()), new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()), recipe.result(), recipe.ingredients, partCount, extraMaterials);
  }

  public Identifier getId() {
    return id;
  }

  @Override
  public List<Ingredient> getParts() {
    return ingredients;
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory) {
    return ShapedMaterialsRecipe.assemble(super.assemble(inventory), inventory, ingredients, partCount, false, extraMaterials);
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeSerializer<ShapelessRecipe> getSerializer() {
    return (RecipeSerializer<ShapelessRecipe>)(RecipeSerializer<?>) TinkerTables.shapelessMaterialsRecipeSerializer.get();
  }

  public static class Serializer implements LoggingRecipeSerializer<ShapelessMaterialsRecipe> {
    static final Loadable<List<MaterialVariantId>> EXTRA_MATERIALS = ShapedMaterialsRecipe.Serializer.EXTRA_MATERIALS;
    static final LoadableField<List<MaterialVariantId>,ShapelessMaterialsRecipe> MATERIAL_FIELD = EXTRA_MATERIALS.defaultField("extra_materials", List.of(), r -> r.extraMaterials);

    @Override
    public ShapelessMaterialsRecipe fromJson(Identifier recipeId, JsonObject json) {
      ShapelessRecipe vanilla = SHAPELESS_RECIPE.fromJson(recipeId, json);
      int parts = GsonHelper.getAsInt(json, "parts");
      if (parts < 1 || parts > vanilla.ingredients.size()) {
        throw new JsonSyntaxException("Parts must be between 1 and the number of ingredients " + vanilla.ingredients.size());
      }
      return new ShapelessMaterialsRecipe(recipeId, vanilla, parts, MATERIAL_FIELD.get(json));
    }

    @Override
    @Nullable
    public ShapelessMaterialsRecipe fromNetworkSafe(Identifier recipeId, FriendlyByteBuf buffer) {
      ShapelessRecipe recipe = SHAPELESS_RECIPE.fromNetwork(recipeId, buffer);
      return recipe == null ? null : new ShapelessMaterialsRecipe(recipeId, recipe, buffer.readByte(), MATERIAL_FIELD.decode(buffer));
    }

    @Override
    public void toNetworkSafe(FriendlyByteBuf buffer, ShapelessMaterialsRecipe recipe) {
      SHAPELESS_RECIPE.toNetwork(buffer, recipe);
      buffer.writeByte(recipe.partCount);
      MATERIAL_FIELD.encode(buffer, recipe);
    }
  }
}
