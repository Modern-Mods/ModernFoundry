package modernmods.modernfoundry.world.data;

import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.data.PackOutput;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import modernmods.mantle.recipe.data.ICommonRecipeHelper;
import modernmods.modernfoundry.common.data.BaseRecipeProvider;
import modernmods.modernfoundry.common.json.ConfigEnabledCondition;
import modernmods.modernfoundry.common.registration.GeodeItemObject;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.shared.block.SlimeType;
import modernmods.modernfoundry.world.TinkerWorld;

import java.util.function.Consumer;

public class WorldRecipeProvider extends BaseRecipeProvider implements ICommonRecipeHelper {
  public WorldRecipeProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct World Recipes";
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    // Add recipe for all slimeball <-> congealed and slimeblock <-> slimeball
    // only earth slime recipe we need here slime
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.BUILDING_BLOCKS, TinkerWorld.congealedSlime.get(SlimeType.EARTH))
                       .define('#', SlimeType.EARTH.getSlimeballTag())
                       .pattern("##")
                       .pattern("##")
                       .unlockedBy("has_item", has(SlimeType.EARTH.getSlimeballTag()))
                       .group("modernfoundry:congealed_slime")
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location("common/slime/earth/congealed")));

    // does not need green as its the fallback
    for (SlimeType slimeType : SlimeType.TINKER) {
      Identifier name = location("common/slime/" + slimeType.getSerializedName() + "/congealed");
      ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.BUILDING_BLOCKS, TinkerWorld.congealedSlime.get(slimeType))
                         .define('#', slimeType.getSlimeballTag())
                         .pattern("##")
                         .pattern("##")
                         .unlockedBy("has_item", has(slimeType.getSlimeballTag()))
                         .group("modernfoundry:congealed_slime")
                         .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(name));
      Identifier blockName = location("common/slime/" + slimeType.getSerializedName() + "/slimeblock");
      ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.REDSTONE, TinkerWorld.slime.get(slimeType))
                         .define('#', slimeType.getSlimeballTag())
                         .pattern("###")
                         .pattern("###")
                         .pattern("###")
                         .unlockedBy("has_item", has(slimeType.getSlimeballTag()))
                         .group("slime_blocks")
                         .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(blockName));
      // green already can craft into slime balls
      ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.MISC, TinkerCommons.slimeball.get(slimeType), 9)
                            .requires(TinkerWorld.slime.get(slimeType))
                            .unlockedBy("has_item", has(TinkerWorld.slime.get(slimeType)))
                            .group("modernfoundry:slime_balls")
                            .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId("modernfoundry:common/slime/" + slimeType.getSerializedName() + "/slimeball_from_block"));
    }
    // all types of congealed need a recipe to a block
    for (SlimeType slimeType : SlimeType.values()) {
      ShapelessRecipeBuilder.shapeless(ITEM_LOOKUP, RecipeCategory.MISC, TinkerCommons.slimeball.get(slimeType), 4)
                            .requires(TinkerWorld.congealedSlime.get(slimeType))
                            .unlockedBy("has_item", has(TinkerWorld.congealedSlime.get(slimeType)))
                            .group("modernfoundry:slime_balls")
                            .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId("modernfoundry:common/slime/" + slimeType.getSerializedName() + "/slimeball_from_congealed"));
    }

    // craft other slime based items, forge does not automatically add recipes using the tag anymore
    Consumer<FinishedRecipe> slimeConsumer = withCondition(consumer, ConfigEnabledCondition.SLIME_RECIPE_FIX);
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.REDSTONE, Blocks.STICKY_PISTON)
                       .pattern("#")
                       .pattern("P")
                       .define('#', Tags.Items.SLIME_BALLS)
                       .define('P', Blocks.PISTON)
                       .unlockedBy("has_slime_ball", has(Tags.Items.SLIME_BALLS))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(slimeConsumer), recipeId(location("common/slime/sticky_piston")));
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.TOOLS, Items.LEAD, 2)
                       .define('~', Items.STRING)
                       .define('O', Tags.Items.SLIME_BALLS)
                       .pattern("~~ ")
                       .pattern("~O ")
                       .pattern("  ~")
                       .unlockedBy("has_slime_ball", has(Tags.Items.SLIME_BALLS))
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(slimeConsumer), recipeId(location("common/slime/lead")));

    // wood
    String woodFolder = "world/wood/";
    woodCrafting(consumer, TinkerWorld.greenheart, woodFolder + "greenheart/");
    woodCrafting(consumer, TinkerWorld.skyroot, woodFolder + "skyroot/");
    woodCrafting(consumer, TinkerWorld.bloodshroom, woodFolder + "bloodshroom/");
    woodCrafting(consumer, TinkerWorld.enderbark, woodFolder + "enderbark/");

    // geodes
    geodeRecipes(consumer, TinkerWorld.earthGeode, SlimeType.EARTH, "common/slime/earth/");
    geodeRecipes(consumer, TinkerWorld.skyGeode,   SlimeType.SKY,   "common/slime/sky/");
    geodeRecipes(consumer, TinkerWorld.ichorGeode, SlimeType.ICHOR, "common/slime/ichor/");
    geodeRecipes(consumer, TinkerWorld.enderGeode, SlimeType.ENDER, "common/slime/ender/");
  }

  private void geodeRecipes(Consumer<FinishedRecipe> consumer, GeodeItemObject geode, SlimeType slime, String folder) {
    ShapedRecipeBuilder.shaped(ITEM_LOOKUP, RecipeCategory.BUILDING_BLOCKS, geode.getBlock())
                       .define('#', geode.asItem())
                       .pattern("##")
                       .pattern("##")
                       .unlockedBy("has_item", has(geode.asItem()))
                       .group("modernfoundry:slime_crystal_block")
                       .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "crystal_block")));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(geode), RecipeCategory.MISC, CookingBookCategory.MISC, TinkerCommons.slimeball.get(slime), 0.2f, 200)
                              .unlockedBy("has_crystal", has(geode))
                              .group("modernfoundry:slime_crystal")
                              .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "crystal_smelting")));
    ItemLike dirt = TinkerWorld.slimeDirt.get(slime.asDirt());
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(dirt), RecipeCategory.MISC, CookingBookCategory.MISC, geode, 0.2f, 400)
                              .unlockedBy("has_dirt", has(dirt))
                              .group("modernfoundry:slime_dirt")
                              .save(modernmods.mantle.recipe.data.VanillaFinishedRecipe.output(consumer), recipeId(location(folder + "crystal_growing")));
  }
}
