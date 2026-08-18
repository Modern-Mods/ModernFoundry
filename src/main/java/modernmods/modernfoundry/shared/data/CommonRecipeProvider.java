package modernmods.modernfoundry.shared.data;

import net.minecraft.world.item.crafting.CookingBookCategory;
import modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient;
import net.minecraft.data.PackOutput;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.neoforged.neoforge.common.Tags;
import modernmods.mantle.recipe.data.ConsumerWrapperBuilder;
import modernmods.mantle.recipe.data.ICommonRecipeHelper;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.data.BaseRecipeProvider;
import modernmods.modernfoundry.common.json.ConfigEnabledCondition;
import modernmods.modernfoundry.fluids.TinkerFluids;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.recipe.casting.ItemCastingRecipeBuilder;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.shared.TinkerMaterials;
import modernmods.modernfoundry.shared.block.ClearStainedGlassBlock.GlassColor;
import modernmods.modernfoundry.shared.block.SlimeType;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.tables.TinkerTables;
import modernmods.modernfoundry.world.TinkerWorld;

import java.util.Locale;
import java.util.function.Consumer;

public class CommonRecipeProvider extends BaseRecipeProvider implements ICommonRecipeHelper {
  public CommonRecipeProvider(PackOutput output) {
    super(output);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Common Recipes";
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    this.addCommonRecipes(consumer);
    this.addMaterialRecipes(consumer);
  }

  private void addCommonRecipes(Consumer<FinishedRecipe> consumer) {
    // firewood and lavawood
    String folder = "common/firewood/";
    slabStairsCrafting(consumer, TinkerMaterials.blazewood, folder, false);
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerMaterials.blazewood.getFence(), 6)
                       .pattern("WWW").pattern("WWW")
                       .define('W', TinkerMaterials.blazewood)
                       .unlockedBy("has_planks", has(TinkerMaterials.blazewood))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "blazewood_fence")));

    // nahuatl
    slabStairsCrafting(consumer, TinkerMaterials.nahuatl, folder, false);
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerMaterials.nahuatl.getFence(), 6)
                       .pattern("WWW").pattern("WWW")
                       .define('W', TinkerMaterials.nahuatl)
                       .unlockedBy("has_planks", has(TinkerMaterials.nahuatl))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "nahuatl_fence")));

    // gold
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerCommons.goldBars, 16)
                       .define('#', Tags.Items.INGOTS_GOLD)
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_ingot", has(Tags.Items.INGOTS_GOLD))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/gold_bars")));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerCommons.goldPlatform, 4)
                       .define('#', Tags.Items.INGOTS_GOLD)
                       .define('.', Tags.Items.NUGGETS_GOLD)
                       .pattern("#.#")
                       .pattern(". .")
                       .pattern("#.#")
                       .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/gold_platform")));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerCommons.ironPlatform, 4)
                       .define('#', Tags.Items.INGOTS_IRON)
                       .define('.', Tags.Items.NUGGETS_IRON)
                       .pattern("#.#")
                       .pattern(". .")
                       .pattern("#.#")
                       .unlockedBy("has_bars", has(Tags.Items.INGOTS_IRON))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/iron_platform")));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerCommons.copperPlatform.get(WeatherState.UNAFFECTED), 4)
                       .define('#', Tags.Items.INGOTS_COPPER)
                       .define('.', TinkerTags.Items.NUGGETS_COPPER)
                       .pattern("#.#")
                       .pattern(". .")
                       .pattern("#.#")
                       .unlockedBy("has_bars", has(Tags.Items.INGOTS_COPPER))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/copper_platform")));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerCommons.cobaltPlatform, 4)
                       .define('#', TinkerMaterials.cobalt.getIngotTag())
                       .define('.', TinkerMaterials.cobalt.getNuggetTag())
                       .pattern("#.#")
                       .pattern(". .")
                       .pattern("#.#")
                       .unlockedBy("has_bars", has(TinkerMaterials.cobalt.getIngotTag()))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/cobalt_platform")));
    TinkerCommons.waxedCopperPlatform.forEach((age, block) -> {
      Block unwaxed = TinkerCommons.copperPlatform.get(age);
      ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.DECORATIONS, block)
                            .requires(unwaxed)
                            .requires(Items.HONEYCOMB)
                            .group("modernfoundry:wax_copper_platform")
                            .unlockedBy("has_block", has(unwaxed))
                            .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/copper_platform_waxing_" + age.toString().toLowerCase(Locale.ROOT))));
    });



    // book
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.MISC, TinkerCommons.materialsAndYou)
                          .requires(Items.BOOK)
                          .requires(TinkerTables.pattern)
                          .unlockedBy("has_item", has(TinkerTables.pattern))
                          .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(TinkerCommons.materialsAndYou, "common/")));
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.MISC, TinkerCommons.tinkersGadgetry)
                          .requires(Items.BOOK)
                          .requires(SlimeType.SKY.getSlimeballTag())
                          .unlockedBy("has_item", has(SlimeType.SKY.getSlimeballTag()))
                          .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(TinkerCommons.tinkersGadgetry, "common/")));
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.MISC, TinkerCommons.punySmelting)
                          .requires(Items.BOOK)
                          .requires(TinkerSmeltery.grout)
                          .unlockedBy("has_item", has(TinkerSmeltery.grout))
                          .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(TinkerCommons.punySmelting, "common/")));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.mightySmelting)
                            .setFluidAndTime(TinkerFluids.searedStone, FluidValues.BRICK)
                            .setCast(Items.BOOK, true)
                            .save(consumer, prefix(TinkerCommons.mightySmelting, "common/"));
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.MISC, TinkerCommons.fantasticFoundry)
                          .requires(Items.BOOK)
                          .requires(TinkerSmeltery.netherGrout)
                          .unlockedBy("has_item", has(TinkerSmeltery.netherGrout))
                          .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(TinkerCommons.fantasticFoundry, "common/")));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.encyclopedia)
                            .setFluidAndTime(TinkerFluids.moltenGold, FluidValues.INGOT)
                            .setCast(Items.BOOK, true)
                            .save(consumer, prefix(TinkerCommons.encyclopedia, "common/"));

    // glass
    folder = "common/glass/";
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, TinkerCommons.clearGlassPane, 16)
                       .define('#', TinkerCommons.clearGlass)
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_block", has(TinkerCommons.clearGlass))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(TinkerCommons.clearGlassPane, folder)));
    for (GlassColor color : GlassColor.values()) {
      Block block = TinkerCommons.clearStainedGlass.get(color);
      ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.BUILDING_BLOCKS, block, 8)
                         .define('#', TinkerCommons.clearGlass)
                         .define('X', color.getDye().getTag())
                         .pattern("###")
                         .pattern("#X#")
                         .pattern("###")
                         .group(prefix("stained_clear_glass"))
                         .unlockedBy("has_clear_glass", has(TinkerCommons.clearGlass))
                         .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(id(block), folder)));
      Block pane = TinkerCommons.clearStainedGlassPane.get(color);
      Identifier paneId = id(pane);
      ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, pane, 16)
                         .define('#', block)
                         .pattern("###")
                         .pattern("###")
                         .group(prefix("stained_clear_glass_pane"))
                         .unlockedBy("has_block", has(block))
                         .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(prefix(paneId, folder)));
      ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, pane, 8)
                         .define('#', TinkerCommons.clearGlassPane)
                         .define('X', color.getDye().getTag())
                         .pattern("###")
                         .pattern("#X#")
                         .pattern("###")
                         .group(prefix("stained_clear_glass_pane"))
                         .unlockedBy("has_clear_glass", has(TinkerCommons.clearGlassPane))
                         .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(wrap(paneId, folder, "_from_panes")));
    }
    // fix vanilla recipes not using tinkers glass
    String glassVanillaFolder = folder + "vanilla/";
    Consumer<FinishedRecipe> vanillaGlassConsumer = withCondition(consumer, ConfigEnabledCondition.GLASS_RECIPE_FIX);
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.MISC, Blocks.BEACON)
                       .define('S', Items.NETHER_STAR)
                       .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                       .define('O', Blocks.OBSIDIAN)
                       .pattern("GGG")
                       .pattern("GSG")
                       .pattern("OOO")
                       .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(vanillaGlassConsumer), recipeId(prefix(id(Blocks.BEACON), glassVanillaFolder)));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.REDSTONE, Blocks.DAYLIGHT_DETECTOR)
                       .define('Q', Items.QUARTZ)
                       .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                       .define('W', ItemTags.WOODEN_SLABS)
                       .pattern("GGG")
                       .pattern("QQQ")
                       .pattern("WWW")
                       .unlockedBy("has_quartz", has(Items.QUARTZ))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(vanillaGlassConsumer), recipeId(prefix(id(Blocks.DAYLIGHT_DETECTOR), glassVanillaFolder)));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.DECORATIONS, Items.END_CRYSTAL)
                       .define('T', Items.GHAST_TEAR)
                       .define('E', Items.ENDER_EYE)
                       .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                       .pattern("GGG")
                       .pattern("GEG")
                       .pattern("GTG")
                       .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(vanillaGlassConsumer), recipeId(prefix(id(Items.END_CRYSTAL), glassVanillaFolder)));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.BREWING, Items.GLASS_BOTTLE, 3)
                       .define('#', Tags.Items.GLASS_BLOCKS_COLORLESS)
                       .pattern("# #")
                       .pattern(" # ")
                       .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS_COLORLESS))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(vanillaGlassConsumer), recipeId(prefix(id(Items.GLASS_BOTTLE), glassVanillaFolder)));


    // vanilla recipes
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.MISC, Items.FLINT)
                          .requires(Blocks.GRAVEL)
                          .requires(Blocks.GRAVEL)
                          .requires(Blocks.GRAVEL)
                          .unlockedBy("has_item", has(Blocks.GRAVEL))
                          .save(
                            modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(ConsumerWrapperBuilder.wrap()
                                                  .addCondition(ConfigEnabledCondition.GRAVEL_TO_FLINT)
                                                  .build(consumer)),
                            recipeId(location("common/flint")));

    // allow crafting the blast furnace in the nether
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.BUILDING_BLOCKS, Blocks.BLAST_FURNACE)
                       .define('#', Blocks.SMOOTH_BASALT)
                       .define('X', Blocks.FURNACE)
                       .define('I', Items.IRON_INGOT)
                       .pattern("III")
                       .pattern("IXI")
                       .pattern("###")
                       .unlockedBy("has_smooth_stone", has(Blocks.SMOOTH_BASALT))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/basalt_blast_furnace")));

    // cheese
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.FOOD, TinkerCommons.cheeseBlock)
                       .define('#', TinkerCommons.cheeseIngot)
                       .pattern("##").pattern("##")
                       .unlockedBy("has_cheese", has(TinkerCommons.cheeseIngot))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/cheese_block_from_ingot")));
    ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.FOOD, TinkerCommons.cheeseIngot, 4)
                          .requires(TinkerCommons.cheeseBlock)
                          .unlockedBy("has_cheese", has(TinkerCommons.cheeseBlock))
                          .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/cheese_ingot_from_block")));
  }

  private void addMaterialRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "common/materials/";

    // ores
    metalCrafting(consumer, TinkerMaterials.cobalt, folder);
    metalCrafting(consumer, TinkerMaterials.steel, folder);
    // tier 3
    metalCrafting(consumer, TinkerMaterials.slimesteel, folder);
    metalCrafting(consumer, TinkerMaterials.amethystBronze, folder);
    metalCrafting(consumer, TinkerMaterials.roseGold, folder);
    metalCrafting(consumer, TinkerMaterials.pigIron, folder);
    // tier 4
    metalCrafting(consumer, TinkerMaterials.cinderslime, folder);
    metalCrafting(consumer, TinkerMaterials.queensSlime, folder);
    metalCrafting(consumer, TinkerMaterials.manyullyn, folder);
    metalCrafting(consumer, TinkerMaterials.hepatizon, folder);
    metalCrafting(consumer, TinkerMaterials.knightmetal, folder);
    metalCrafting(consumer, TinkerMaterials.knightslime, folder);
    // custom nuggets
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.COPPER_INGOT,    "nugget", TinkerMaterials.copperNugget,    TinkerTags.Items.NUGGETS_COPPER, folder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.NETHERITE_SCRAP, "nugget", TinkerMaterials.debrisNugget,    TinkerTags.Items.NUGGETS_NETHERITE_SCRAP, folder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.NETHERITE_INGOT, "nugget", TinkerMaterials.netheriteNugget, TinkerTags.Items.NUGGETS_NETHERITE, folder);

    // smelt ore into ingots, must use a blast furnace for nether ores
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.rawCobalt, TinkerWorld.cobaltOre), RecipeCategory.MISC, CookingBookCategory.MISC, TinkerMaterials.cobalt.getIngot(), 1.5f, 200)
      .unlockedBy("has_item", has(TinkerWorld.rawCobalt))
      .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "cobalt_ingot_blasting")));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.cobaltShard), RecipeCategory.MISC, CookingBookCategory.MISC, TinkerMaterials.cobalt.getNugget(), 0.2f, 50)
      .unlockedBy("has_item", has(TinkerWorld.cobaltShard))
      .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "cobalt_nugget_blasting")));
    // steel can use either furnace
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerWorld.steelShard), RecipeCategory.MISC, CookingBookCategory.MISC, TinkerMaterials.steel.getNugget(), 0.2f, 50)
      .unlockedBy("has_item", has(TinkerWorld.steelShard))
      .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "steel_nugget_smelting")));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.steelShard), RecipeCategory.MISC, CookingBookCategory.MISC, TinkerMaterials.steel.getNugget(), 0.2f, 25)
      .unlockedBy("has_item", has(TinkerWorld.steelShard))
      .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "steel_nugget_blasting")));
    // knightmetal - normally would not use the tag, but we know TF does not provide nuggets for armor shards
    SimpleCookingRecipeBuilder.smelting(LazyTagIngredient.of(TinkerTags.Items.KNIGHTMETAL_SHARD), RecipeCategory.MISC, CookingBookCategory.MISC, TinkerMaterials.knightmetal.getNugget(), 0.2f, 50)
      .unlockedBy("has_item", has(TinkerWorld.knightmetalShard))
      .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "knightmetal_nugget_smelting")));
    SimpleCookingRecipeBuilder.blasting(LazyTagIngredient.of(TinkerTags.Items.KNIGHTMETAL_SHARD), RecipeCategory.MISC, CookingBookCategory.MISC, TinkerMaterials.knightmetal.getNugget(), 0.2f, 25)
      .unlockedBy("has_item", has(TinkerWorld.knightmetalShard))
      .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "knightmetal_nugget_blasting")));

    // pack raw cobalt
    packingRecipe(consumer, RecipeCategory.MISC, "raw_block", TinkerWorld.rawCobaltBlock, "raw", TinkerWorld.rawCobalt, TinkerTags.Items.RAW_COBALT, folder);
  }
}
