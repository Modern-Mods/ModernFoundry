package modernmods.modernfoundry.gadgets.data;

import net.minecraft.world.item.crafting.CookingBookCategory;
import modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.PackOutput;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.data.BaseRecipeProvider;
import modernmods.modernfoundry.fluids.TinkerFluids;
import modernmods.modernfoundry.gadgets.TinkerGadgets;
import modernmods.modernfoundry.gadgets.entity.FrameType;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.recipe.casting.ItemCastingRecipeBuilder;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.shared.TinkerMaterials;
import modernmods.modernfoundry.shared.block.SlimeType;
import modernmods.modernfoundry.world.TinkerWorld;
import modernmods.modernfoundry.world.block.FoliageType;

import java.util.function.Consumer;

public class GadgetRecipeProvider extends BaseRecipeProvider {
  public GadgetRecipeProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Gadget Recipes";
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    // piggybackpack
    String folder = "gadgets/";
    ItemCastingRecipeBuilder.tableRecipe(TinkerGadgets.piggyBackpack)
                            .setCast(Items.SADDLE, true)
                            .setFluidAndTime(TinkerFluids.skySlime, FluidValues.SLIMEBALL * 4)
                            .save(consumer, prefix(TinkerGadgets.piggyBackpack, folder));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerGadgets.punji)
                       .define('b', Items.BAMBOO)
                       .pattern(" b ")
                       .pattern("bbb")
                       .unlockedBy("has_item", has(Items.BAMBOO))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(TinkerGadgets.punji, folder)));

    // frames
    folder = "gadgets/fancy_frame/";
    frameCrafting(consumer, Tags.Items.NUGGETS_GOLD, FrameType.GOLD);
    frameCrafting(consumer, TinkerMaterials.manyullyn.getNuggetTag(), FrameType.MANYULLYN);
    frameCrafting(consumer, TinkerTags.Items.NUGGETS_NETHERITE, FrameType.NETHERITE);
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerGadgets.itemFrame.get(FrameType.DIAMOND))
                       .define('e', TinkerCommons.obsidianPane)
                       .define('M', Tags.Items.GEMS_DIAMOND)
                       .pattern(" e ")
                       .pattern("eMe")
                       .pattern(" e ")
                       .unlockedBy("has_item", has(Tags.Items.GEMS_DIAMOND))
                       .group(prefix("fancy_item_frame"))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("gadgets/frame/" + FrameType.DIAMOND.getSerializedName())));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerGadgets.itemFrame.get(FrameType.CLEAR))
                       .define('e', Tags.Items.GLASS_PANES_COLORLESS)
                       .define('M', Tags.Items.GLASS_BLOCKS_COLORLESS)
                       .pattern(" e ")
                       .pattern("eMe")
                       .pattern(" e ")
                       .unlockedBy("has_item", has(Tags.Items.GLASS_PANES_COLORLESS))
                       .group(prefix("fancy_item_frame"))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + FrameType.CLEAR.getSerializedName())));
    Item goldFrame = TinkerGadgets.itemFrame.get(FrameType.GOLD);
    Item reversedFrame = TinkerGadgets.itemFrame.get(FrameType.REVERSED_GOLD);
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.DECORATIONS, reversedFrame)
                          .requires(goldFrame)
                          .requires(Items.REDSTONE_TORCH)
                          .unlockedBy("has_item", has(goldFrame))
                          .group(prefix("reverse_fancy_item_frame"))
                          .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + FrameType.REVERSED_GOLD.getSerializedName())));
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.DECORATIONS, goldFrame)
                          .requires(reversedFrame)
                          .requires(Items.REDSTONE_TORCH)
                          .unlockedBy("has_item", has(reversedFrame))
                          .group(prefix("reverse_fancy_item_frame"))
                          .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "reversed_reversed_gold")));

    String cakeFolder = "gadgets/cake/";
    TinkerGadgets.cake.forEach((foliage, cake) -> {
      if (foliage != FoliageType.ICHOR) {
        SlimeType slime = foliage.asSlime();
        ItemLike grass = TinkerWorld.slimeTallGrass.get(foliage);
        ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.FOOD, cake)
                           .define('M', slime != null ? TinkerFluids.slime.get(slime).getBucket() : TinkerFluids.honey.asItem())
                           .define('S', foliage.isNether()
                             ? LazyTagIngredient.of(Tags.Items.DUSTS_GLOWSTONE)
                             : foliage == FoliageType.ENDER ? LazyTagIngredient.of(Tags.Items.DUSTS_REDSTONE) : Ingredient.of(Items.SUGAR))
                           .define('E', Items.EGG)
                           .define('W', TinkerWorld.slimeTallGrass.get(foliage))
                           .pattern("MMM").pattern("SES").pattern("WWW")
                           .unlockedBy("has_slime", has(grass))
                           .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(cakeFolder + foliage.getSerializedName())));
      }
    });
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.FOOD, TinkerGadgets.cake.get(FoliageType.ICHOR))
      .define('M', TinkerFluids.ichor)
      .define('S', LazyTagIngredient.of(Tags.Items.DUSTS_GLOWSTONE))
      .define('E', Items.EGG)
      .define('W', Blocks.WARPED_ROOTS) // TODO: switch to ichor foliage one day
      .pattern("WWW").pattern("SES").pattern("MMM")
      .unlockedBy("has_slime", has(TinkerFluids.ichor))
      .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(cakeFolder + "ichor")));
    Item bucket = TinkerFluids.magma.asItem();
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.FOOD, TinkerGadgets.magmaCake)
                       .define('M', bucket)
                       .define('S', LazyTagIngredient.of(Tags.Items.DUSTS_GLOWSTONE))
                       .define('E', Items.EGG)
                       .define('W', Blocks.CRIMSON_ROOTS)
                       .pattern("MMM").pattern("SES").pattern("WWW")
                       .unlockedBy("has_slime", has(bucket))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(cakeFolder + "magma")));
  }


  /* Helpers */

  /**
   * Adds a recipe to the campfire, furnace, and smoker
   * @param consumer    Recipe consumer
   * @param input       Recipe input
   * @param output      Recipe output
   * @param experience  Experience for the recipe
   * @param folder      Folder to store the recipe
   */
  private void foodCooking(Consumer<FinishedRecipe> consumer, ItemLike input, ItemLike output, float experience, String folder) {
    SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(input), RecipeCategory.FOOD, output, experience, 600)
                              .unlockedBy("has_item", has(input))
                              .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(wrap(id(output), folder, "_campfire")));
    // furnace is 200 ticks
    Identifier outputId = id(output);
    Criterion<?> criteria = has(input);
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.FOOD, CookingBookCategory.FOOD, output, experience, 200)
                              .unlockedBy("has_item", criteria)
                              .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(wrap(outputId, folder, "_furnace")));
    // smoker 100 ticks
    SimpleCookingRecipeBuilder.smoking(Ingredient.of(input), RecipeCategory.FOOD, output, experience, 100)
                              .unlockedBy("has_item", criteria)
                              .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(wrap(outputId, folder, "_smoker")));
  }

  /**
   * Adds a recipe for an item frame type
   * @param consumer  Recipe consumer
   * @param edges     Edge item
   * @param type      Frame type
   */
  private void frameCrafting(Consumer<FinishedRecipe> consumer, TagKey<Item> edges, FrameType type) {
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerGadgets.itemFrame.get(type))
                       .define('e', edges)
                       .define('M', TinkerCommons.obsidianPane)
                       .pattern(" e ")
                       .pattern("eMe")
                       .pattern(" e ")
                       .unlockedBy("has_item", has(edges))
                       .group(prefix("fancy_item_frame"))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("gadgets/frame/" + type.getSerializedName())));
  }
}
