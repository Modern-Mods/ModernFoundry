package modernmods.modernfoundry.library.recipe.casting.material;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicate;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Builder for a composite part recipe, should exist for each part */
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "composite")
public class CompositeCastingRecipeBuilder extends AbstractRecipeBuilder<CompositeCastingRecipeBuilder> {
  private final IMaterialItem result;
  private final int itemCost;
  @Setter @Nullable
  private MaterialStatsId castingStatConflict = null;
  private final TypeAwareRecipeSerializer<? extends CompositeCastingRecipe> serializer;
  @Setter
  private IJsonPredicate<MaterialVariantId> allowedMaterials = MaterialPredicate.ANY;

  public static CompositeCastingRecipeBuilder basin(IMaterialItem result, int itemCost) {
    return composite(result, itemCost, TinkerSmeltery.basinCompositeSerializer);
  }

  public static CompositeCastingRecipeBuilder table(IMaterialItem result, int itemCost) {
    return composite(result, itemCost, TinkerSmeltery.tableCompositeSerializer);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(result.asItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    Identifier advancementId = this.buildOptionalAdvancement(id, "casting");
    consumer.accept(new LoadableFinishedRecipe<>(id, new CompositeCastingRecipe(serializer, id, group, itemCost, result, allowedMaterials, castingStatConflict), CompositeCastingRecipe.LOADER, advancementId));
  }

  private class Finished extends AbstractFinishedRecipe {
    public Finished(Identifier ID, @Nullable Identifier advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (!group.isEmpty()) {
        json.addProperty("group", group);
      }
      json.addProperty("result", BuiltInRegistries.ITEM.getKey(result.asItem()).toString());
      json.addProperty("item_cost", itemCost);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return serializer.serializer();
    }
  }
}
