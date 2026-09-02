package modernmods.modernfoundry.library.data.recipe;

import modernmods.hilt.recipe.data.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import modernmods.hilt.recipe.data.IRecipeHelper;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.registration.CastItemObject;
import modernmods.modernfoundry.fluids.TinkerFluids;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.recipe.casting.ItemCastingRecipeBuilder;
import modernmods.modernfoundry.library.recipe.molding.MoldingRecipeBuilder;
import modernmods.modernfoundry.library.recipe.partbuilder.ItemPartRecipeBuilder;

import java.util.function.Consumer;

/**
 * Shared methods between {@link ISmelteryRecipeHelper} and {@link IToolRecipeHelper}
 */
public interface ICastCreationHelper extends IRecipeHelper {
  /* Cast creation */

  /**
   * Adds recipe to create a cast
   * @param consumer  Recipe consumer
   * @param input     Item consumed to create cast
   * @param cast      Produced cast
   * @param folder    Output folder
   */
  default void castCreation(Consumer<FinishedRecipe> consumer, TagKey<Item> input, CastItemObject cast, String folder) {
    castCreation(consumer, Ingredient.of(input), cast, folder, input.location().getPath());
  }

  /**
   * Adds recipe to create a cast
   * @param consumer  Recipe consumer
   * @param input     Item consumed to create cast
   * @param cast      Produced cast
   * @param folder    Output folder
   * @param name      Cast name
   */
  default void castCreation(Consumer<FinishedRecipe> consumer, Ingredient input, CastItemObject cast, String folder, String name) {
    ItemCastingRecipeBuilder.tableRecipe(cast)
                            .setFluidAndTime(TinkerFluids.moltenGold, FluidValues.INGOT)
                            .setCast(input, true)
                            .setSwitchSlots()
                            .save(consumer, location(folder + "gold/" + name));
    // make sand casts via molding in the casting table
    MoldingRecipeBuilder.moldingTable(cast.getSand())
                        .setMaterial(TinkerTags.Items.SAND_CASTS)
                        .setPattern(input, false)
                        .save(consumer, location(folder + "sand/molding/" + name));
    MoldingRecipeBuilder.moldingTable(cast.getRedSand())
                        .setMaterial(TinkerTags.Items.RED_SAND_CASTS)
                        .setPattern(input, false)
                        .save(consumer, location(folder + "red_sand/molding/" + name));
    // make sand casts in the part builder
    ResourceLocation pattern = cast.getName();
    ItemPartRecipeBuilder.item(pattern, ItemOutput.fromItem(cast.getSand()))
                         .setPatternItem(Ingredient.of(TinkerTags.Items.SAND_CASTS))
                         .save(consumer, location(folder + "sand/builder_cast/" + name));
    ItemPartRecipeBuilder.item(pattern, ItemOutput.fromItem(cast.getRedSand()))
                         .setPatternItem(Ingredient.of(TinkerTags.Items.RED_SAND_CASTS))
                         .save(consumer, location(folder + "red_sand/builder_cast/" + name));
    ItemPartRecipeBuilder.item(pattern, ItemOutput.fromItem(cast.getSand(), 4))
                         .setPatternItem(Ingredient.of(Tags.Items.SANDS))
                         .save(consumer, location(folder + "sand/builder_block/" + name));
    ItemPartRecipeBuilder.item(pattern, ItemOutput.fromItem(cast.getRedSand(), 4))
                         .setPatternItem(Ingredient.of(Tags.Items.SANDS))
                         .save(consumer, location(folder + "red_sand/builder_block/" + name));
  }
}
