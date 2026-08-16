package modernmods.modernfoundry.library.recipe.casting.material;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.hilt.recipe.data.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.recipe.data.AbstractRecipeBuilder;
import modernmods.hilt.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicate;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import java.util.function.Consumer;

/** Builder for {@link PartSwapCastingRecipe} */
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "castingRecipe")
public class PartSwapCastingRecipeBuilder extends AbstractRecipeBuilder<PartSwapCastingRecipeBuilder> {
  private final Ingredient tools;
  private final int itemCost;
  private final TypeAwareRecipeSerializer<PartSwapCastingRecipe> recipeSerializer;
  @Setter
  @Accessors(fluent = true)
  private int index = 0;
  @Setter
  private IJsonPredicate<MaterialVariantId> allowedMaterials = MaterialPredicate.ANY;

  /**
   * Creates a new part swapping recipe
   * @param tools     List of tools
   * @param itemCost  Amount needed to cast to swap
   * @return  Builder instance
   */
  public static PartSwapCastingRecipeBuilder basinRecipe(Ingredient tools, int itemCost) {
    return castingRecipe(tools, itemCost, TinkerSmeltery.basinPartSwappingSerializer.get());
  }

  /**
   * Creates a new part swapping recipe
   * @param itemCost  Amount needed to cast to swap
   * @return  Builder instance
   */
  public static PartSwapCastingRecipeBuilder tableRecipe(Ingredient tools, int itemCost) {
    return castingRecipe(tools, itemCost, TinkerSmeltery.tablePartSwappingSerializer.get());
  }

  @SuppressWarnings("deprecation")
  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(tools.getItems()[0].getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    consumer.accept(new LoadableFinishedRecipe<>(id, new PartSwapCastingRecipe(recipeSerializer, id, group, tools, itemCost, index, allowedMaterials), PartSwapCastingRecipe.LOADER, this.buildOptionalAdvancement(id, "materials")));
  }
}
