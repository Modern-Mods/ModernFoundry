package modernmods.modernfoundry.plugin.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import modernmods.modernfoundry.library.recipe.ingredient.MaterialValueIngredient;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipeCache;
import modernmods.modernfoundry.library.recipe.material.ShapedMaterialRecipe;
import modernmods.modernfoundry.plugin.jei.material.MaterialsCraftingExtension;
import modernmods.modernfoundry.plugin.jei.material.ShapedMaterialsExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Logic to show {@link ShapedMaterialRecipe} in JEI
 * @deprecated use {@link ShapedMaterialsExtension}
 */
@Deprecated
public class ShapedMaterialExtension {
  /** Holder-based singleton extension */
  public static final ICraftingCategoryExtension<ShapedMaterialRecipe> INSTANCE = new ICraftingCategoryExtension<>() {
    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<ShapedMaterialRecipe> holder) {
      return holder.value().getIngredients().stream()
        .<SlotDisplay>map(opt -> opt.map(MaterialsCraftingExtension::ingredientDisplay).orElseGet(() -> new SlotDisplay.Composite(List.of())))
        .toList();
    }

    @Override
    public void setRecipe(RecipeHolder<ShapedMaterialRecipe> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
      new ShapedMaterialExtension(holder.value()).setRecipe(builder, craftingGridHelper, focuses);
    }

    // 26.1.2/JEI: getRegistryName was removed from ICraftingCategoryExtension; JEI derives it from the RecipeHolder

    @Override
    public int getWidth(RecipeHolder<ShapedMaterialRecipe> holder) {
      return holder.value().getWidth();
    }

    @Override
    public int getHeight(RecipeHolder<ShapedMaterialRecipe> holder) {
      return holder.value().getHeight();
    }
  };

  private final ShapedMaterialRecipe recipe;
  private final ItemStack plainResult;
  private final List<ItemStack> result;
  private final int[] materialSlots;
  public ShapedMaterialExtension(ShapedMaterialRecipe recipe) {
    this.recipe = recipe;
    MaterialValueIngredient materials = recipe.getMaterial();
    plainResult = MaterialsCraftingExtension.getResult(recipe);
    if (materials != null) {
      this.result = MaterialRecipeCache.getAllRecipes().stream().filter(materials::test).flatMap(mat -> {
        ItemStack stack = plainResult.copy();
        recipe.setMaterial(stack, mat.getMaterial().getVariant());
        // add one copy of the stack per item in the nested ingredient, so the lengths match up
        return IntStream.range(0, mat.getIngredient().items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new).length).mapToObj(i -> stack);
      }).toList();
    } else {
      this.result = List.of(plainResult);
    }
    List<Optional<Ingredient>> inputs = recipe.getIngredients();
    this.materialSlots = IntStream.range(0, inputs.size()).filter(i -> inputs.get(i).map(Ingredient::getCustomIngredient).orElse(null) instanceof MaterialValueIngredient).toArray();
  }

  /** Gets the positional input stacks for the grid */
  private List<List<ItemStack>> getInputStacks() {
    return recipe.getIngredients().stream()
      .map(opt -> opt.map(MaterialsCraftingExtension::ingredientStacks).orElseGet(List::of))
      .toList();
  }

  public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focusGroup) {
    MaterialsCraftingExtension.setRecipe(builder, craftingGridHelper, getInputStacks(), recipe.getWidth(), recipe.getHeight(), recipe, result, plainResult, materialSlots);
  }
}
