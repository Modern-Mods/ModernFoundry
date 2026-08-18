package modernmods.modernfoundry.plugin.jei.material;

import com.google.common.collect.Streams;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.util.context.ContextMap;
import modernmods.mantle.Mantle;
import modernmods.mantle.client.SafeClientAccess;
import modernmods.mantle.plugin.jei.MantleJEIConstants;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipeCache;
import modernmods.modernfoundry.library.recipe.material.MaterialsCraftingTableRecipe;
import modernmods.modernfoundry.library.recipe.material.ShapelessMaterialsRecipe;
import modernmods.modernfoundry.library.tools.helper.ToolBuildHandler;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Common logic for {@link ShapedMaterialsExtension} and {@link ShapelessMaterialsRecipe} display.
 * <p>This is a per-recipe helper; the {@link ICraftingCategoryExtension} singletons delegate their layout logic here.
 */
public class MaterialsCraftingExtension<T extends CraftingRecipe & MaterialsCraftingTableRecipe> {
  /** Holder-based singleton extension for shapeless material recipes */
  public static final ICraftingCategoryExtension<ShapelessMaterialsRecipe> SHAPELESS = new ICraftingCategoryExtension<>() {
    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<ShapelessMaterialsRecipe> holder) {
      return partsDisplays(holder.value().getParts());
    }

    @Override
    public void setRecipe(RecipeHolder<ShapelessMaterialsRecipe> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
      MaterialsCraftingExtension<ShapelessMaterialsRecipe> extension = MaterialsCraftingExtension.shapeless(holder.value());
      if (extension != null) {
        extension.setRecipe(builder, craftingGridHelper, focuses);
      }
    }

    // 26.1.2/JEI: getRegistryName was removed from ICraftingCategoryExtension; JEI derives it from the RecipeHolder
  };

  protected final T recipe;
  private final ItemStack plainResult;
  private final List<ItemStack> result;
  @Nullable
  private final int[] materialSlots;

  public MaterialsCraftingExtension(T recipe) {
    this.recipe = recipe;
    this.plainResult = getResult(recipe);

    // if we have just the one part, set the output to match its material
    if (recipe.getPartCount() == 1) {
      Ingredient firstPart = recipe.getParts().get(0);
      this.result = Arrays.stream(firstPart.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new)).map(variant -> {
        ItemStack stack = plainResult.copy();
        if (variant.getItem() instanceof IMaterialItem materialItem) {
          recipe.setMaterial(stack, materialItem.getMaterial(variant));
        } else {
          recipe.setMaterial(stack, MaterialRecipeCache.findRecipe(variant).getMaterial().getVariant());
        }
        return stack;
      }).toList();
      this.materialSlots = getMaterialSlots(recipe, firstPart);
      // otherwise, use a display material. allow display tool part if it has just 1 material
    } else if (recipe.getExtraMaterials().isEmpty() && plainResult.getItem() instanceof IMaterialItem materialItem) {
      this.result = List.of(materialItem.setMaterialForced(plainResult, ToolBuildHandler.getRenderMaterial(0)));
      this.materialSlots = null;
    } else {
      // display tool
      this.result = List.of(IModifiableDisplay.getDisplayStack(plainResult));
      this.materialSlots = null;
    }
  }

  /** {@return Instance of the shapeless extension, or null if the recipe is invalid for display} */
  @Nullable
  public static MaterialsCraftingExtension<ShapelessMaterialsRecipe> shapeless(ShapelessMaterialsRecipe recipe) {
    List<Ingredient> parts = recipe.getParts();
    for (int i = 0; i < recipe.getPartCount(); i++) {
      if (parts.get(i).items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new).length == 0) {
        return null;
      }
    }
    return new MaterialsCraftingExtension<>(recipe);
  }

  /** Gets the material slots for the given recipe */
  protected int[] getMaterialSlots(T recipe, Ingredient firstPart) {
    return new int[] {0};
  }

  /** Gets the positional input stacks for the grid; shapeless returns a flat list */
  protected List<List<ItemStack>> getInputStacks() {
    return recipe.getParts().stream().map(MaterialsCraftingExtension::ingredientStacks).toList();
  }

  /** Grid width; 0 means shapeless (size is computed) */
  protected int getGridWidth() {
    return 0;
  }

  /** Grid height; 0 means shapeless (size is computed) */
  protected int getGridHeight() {
    return 0;
  }

  /** Sets the recipe in the builder using this instance's computed data */
  public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    setRecipe(builder, craftingGridHelper, getInputStacks(), getGridWidth(), getGridHeight(), recipe, result, plainResult, materialSlots);
  }

  /** Sets the recipe in the builder */
  public static void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, List<List<ItemStack>> inputStacks, int width, int height, Recipe<?> recipe, List<ItemStack> result, ItemStack plainResult, @Nullable int[] materialSlots) {
    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(plainResult);

    // shapeless needs its width and height set, but we also want to recover those sizes, so calculate it locally
    if (width <= 0 || height <= 0) {
      width = height = getShapelessSize(inputStacks.size());
      builder.setShapeless();
    }
    List<IRecipeSlotBuilder> inputs = craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputStacks, width, height);
    IRecipeSlotBuilder output = craftingGridHelper.createAndSetOutputs(builder, result);
    if (inputs.size() != 9) {
      Identifier id = recipe instanceof MaterialsCraftingTableRecipe materialRecipe ? materialRecipe.getId() : null;
      Mantle.logger.error("Failed to create focus link for {} as the layout {} is not 3x3", id, builder.getClass().getName());
    } else if (materialSlots != null) {
      // apply focus links
      int finalWidth = width;
      int finalHeight = height;
      builder.createFocusLink(Streams.concat(
        Stream.of(output),
        Arrays.stream(materialSlots).mapToObj(i -> inputs.get(MantleJEIConstants.getCraftingIndex(i, finalWidth, finalHeight)))
      ).toArray(IRecipeSlotBuilder[]::new));
    }
  }

  /** Resolves the base (materialless) result of a crafting recipe via its display */
  public static ItemStack getResult(Recipe<?> recipe) {
    Level level = Minecraft.getInstance().level;
    if (level == null) {
      return ItemStack.EMPTY;
    }
    List<RecipeDisplay> displays = recipe.display();
    if (displays.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ContextMap context = SlotDisplayContext.fromLevel(level);
    return displays.getFirst().result().resolveForFirstStack(context);
  }

  /** Converts a single ingredient into the list of stacks it accepts */
  public static List<ItemStack> ingredientStacks(Ingredient ingredient) {
    return ingredient.items().<ItemStack>map(ItemStack::new).toList();
  }

  /** Converts an ingredient into a slot display for JEI */
  public static SlotDisplay ingredientDisplay(Ingredient ingredient) {
    return new SlotDisplay.Composite(ingredient.items().<SlotDisplay>map(SlotDisplay.ItemSlotDisplay::new).toList());
  }

  /** Converts a list of ingredients into slot displays for JEI */
  public static List<SlotDisplay> partsDisplays(List<Ingredient> parts) {
    return parts.stream().map(MaterialsCraftingExtension::ingredientDisplay).toList();
  }

  /** Gets the width and height of the grid for a shapeless recipe. */
  private static int getShapelessSize(int total) {
    if (total > 4) {
      return 3;
    } else if (total > 1) {
      return 2;
    } else {
      return 1;
    }
  }
}
