package modernmods.modernfoundry.plugin.jei.material;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import modernmods.modernfoundry.library.recipe.material.ShapedMaterialsRecipe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/** Logic to show {@link ShapedMaterialsRecipe} in JEI */
public class ShapedMaterialsExtension extends MaterialsCraftingExtension<ShapedMaterialsRecipe> {
  /** Holder-based singleton extension */
  public static final ICraftingCategoryExtension<ShapedMaterialsRecipe> INSTANCE = new ICraftingCategoryExtension<>() {
    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<ShapedMaterialsRecipe> holder) {
      return holder.value().getIngredients().stream()
        .<SlotDisplay>map(opt -> opt.map(MaterialsCraftingExtension::ingredientDisplay).orElseGet(() -> new SlotDisplay.Composite(List.of())))
        .toList();
    }

    @Override
    public int getWidth(RecipeHolder<ShapedMaterialsRecipe> holder) {
      return holder.value().getWidth();
    }

    @Override
    public int getHeight(RecipeHolder<ShapedMaterialsRecipe> holder) {
      return holder.value().getHeight();
    }

    @Override
    public void setRecipe(RecipeHolder<ShapedMaterialsRecipe> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
      ShapedMaterialsExtension extension = ShapedMaterialsExtension.create(holder.value());
      if (extension != null) {
        extension.setRecipe(builder, craftingGridHelper, focuses);
      }
    }

    // 26.1.2/JEI: getRegistryName was removed from ICraftingCategoryExtension; JEI derives it from the RecipeHolder
  };

  private ShapedMaterialsExtension(ShapedMaterialsRecipe recipe) {
    super(recipe);
  }

  /** {@return Instance of the shaped extension, or null if the recipe is invalid for display} */
  @Nullable
  public static ShapedMaterialsExtension create(ShapedMaterialsRecipe recipe) {
    for (Ingredient ingredient : recipe.getParts()) {
      if (ingredient.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new).length == 0) {
        return null;
      }
    }
    return new ShapedMaterialsExtension(recipe);
  }

  @Override
  protected int[] getMaterialSlots(ShapedMaterialsRecipe recipe, Ingredient firstPart) {
    List<Optional<Ingredient>> inputs = recipe.getIngredients();
    return IntStream.range(0, inputs.size()).filter(i -> inputs.get(i).orElse(null) == firstPart).toArray();
  }

  @Override
  protected List<List<ItemStack>> getInputStacks() {
    return recipe.getIngredients().stream()
      .map(opt -> opt.map(MaterialsCraftingExtension::ingredientStacks).orElseGet(List::of))
      .toList();
  }

  @Override
  protected int getGridWidth() {
    return recipe.getWidth();
  }

  @Override
  protected int getGridHeight() {
    return recipe.getHeight();
  }
}
