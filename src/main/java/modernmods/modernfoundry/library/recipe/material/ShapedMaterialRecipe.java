package modernmods.modernfoundry.library.recipe.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.Loadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.recipe.helper.LoggingRecipeSerializer;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.recipe.ingredient.MaterialValueIngredient;
import modernmods.modernfoundry.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Shaped recipe with a number of {@link modernmods.modernfoundry.library.recipe.ingredient.MaterialValueIngredient} to set the material of the result.
 * @deprecated use {@link ShapedMaterialsRecipe}, which requires specifying the ingredients for each part.
 */
@Deprecated
public class ShapedMaterialRecipe extends ShapedRecipe {
  private final Identifier id;
  private MaterialValueIngredient material;
  private final List<MaterialVariantId> extraMaterials;
  public ShapedMaterialRecipe(Identifier id, Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result, List<MaterialVariantId> extraMaterials) {
    super(commonInfo, bookInfo, pattern, result);
    this.id = id;
    this.extraMaterials = extraMaterials;
  }

  public ShapedMaterialRecipe(ShapedRecipe recipe, List<MaterialVariantId> extraMaterials) {
    this(LoggingRecipeSerializer.UNKNOWN_ID, new Recipe.CommonInfo(recipe.showNotification()), new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()), recipe.pattern, recipe.result, extraMaterials);
  }

  /** @deprecated use {@link #ShapedMaterialRecipe(ShapedRecipe,List)} */
  @Deprecated(forRemoval = true)
  public ShapedMaterialRecipe(ShapedRecipe recipe) {
    this(recipe, List.of());
  }

  public Identifier getId() {
    return id;
  }

  /** Gets the material to match */
  @Nullable
  public MaterialValueIngredient getMaterial() {
    if (material == null) {
      // assume all material ingredients match the same stat type
      for (Optional<Ingredient> optional : getIngredients()) {
        // collect all ingredients that match
        if (optional.isPresent() && optional.get().getCustomIngredient() instanceof MaterialValueIngredient materialValue) {
          if (material == null) {
            material = materialValue;
          } else {
            // ensure the stat type matches, and expand the range
            material = material.merge(materialValue);
          }
        }
      }
      // if we found no materials, that is also an issue
      if (material == null) {
        TConstruct.LOG.error("No material ingredient found for material shaped recipe {}, this indicates a broken recipe", getId());
      }
    }
    return material;
  }

  @Nullable
  private MaterialVariantId findMaterial(CraftingInput inventory) {
    MaterialValueIngredient material = getMaterial();
    if (material == null) {
      return null;
    }
    // ensure same material in all slots
    MaterialVariantId firstMaterial = null;
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        // ignore anything that does not meet our requirements
        MaterialVariantId matchedMaterial = material.getMaterial(stack);
        if (matchedMaterial != null) {
          // first match is set
          if (firstMaterial == null) {
            firstMaterial = matchedMaterial;
          } else if (!firstMaterial.matchesVariant(matchedMaterial)) {
            // if same material but different variants, just discard the variant
            if (firstMaterial.getId().equals(matchedMaterial.getId())) {
              firstMaterial = firstMaterial.getId();
            } else {
              // if different materials, no match
              return null;
            }
          }
        }
      }
    }
    return firstMaterial;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    if (!super.matches(inventory, level)) {
      return false;
    }

    // must have a material to match, no mixing
    return findMaterial(inventory) != null;
  }

  /** Sets the material for the given stack */
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory) {
    ItemStack stack = super.assemble(inventory);
    MaterialVariantId material = findMaterial(inventory);
    if (material != null) {
      setMaterial(stack, material);
    }
    return stack;
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer<ShapedRecipe>)(RecipeSerializer<?>) TinkerTables.shapedMaterialRecipeSerializer.get();
  }

  public static class Serializer implements LoggingRecipeSerializer<ShapedMaterialRecipe> {
    static final Loadable<List<MaterialVariantId>> EXTRA_MATERIALS = ShapedMaterialsRecipe.Serializer.EXTRA_MATERIALS;
    static final LoadableField<List<MaterialVariantId>,ShapedMaterialRecipe> MATERIAL_FIELD = EXTRA_MATERIALS.defaultField("extra_materials", List.of(), r -> r.extraMaterials);

    @Override
    public ShapedMaterialRecipe fromJson(Identifier recipeId, JsonObject json) {
      ShapedMaterialRecipe recipe = new ShapedMaterialRecipe(SHAPED_RECIPE.fromJson(recipeId, json), MATERIAL_FIELD.get(json));
      // ensure the material is valid, since we have all the needed information to check
      // better now than at runtime
      if (recipe.getMaterial() == null) {
        throw new JsonSyntaxException("Invalid material ingredients for shaped material recipe " + recipeId);
      }
      return recipe;
    }

    @Override
    @Nullable
    public ShapedMaterialRecipe fromNetworkSafe(Identifier recipeId, FriendlyByteBuf buffer) {
      ShapedRecipe recipe = SHAPED_RECIPE.fromNetwork(recipeId, buffer);
      List<MaterialVariantId> extraMaterials = MATERIAL_FIELD.decode(buffer);
      return recipe == null ? null : new ShapedMaterialRecipe(recipe, extraMaterials);
    }

    @Override
    public void toNetworkSafe(FriendlyByteBuf buffer, ShapedMaterialRecipe recipe) {
      SHAPED_RECIPE.toNetwork(buffer, recipe);
      MATERIAL_FIELD.encode(buffer, recipe);
    }
  }
}
