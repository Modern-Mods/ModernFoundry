package modernmods.modernfoundry.library.recipe.molding;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "molding")
public class MoldingRecipeBuilder extends AbstractRecipeBuilder<MoldingRecipeBuilder> {
  private final ItemOutput output;
  private final TypeAwareRecipeSerializer<MoldingRecipe> serializer;
  @javax.annotation.Nullable private Ingredient material = null;
  @javax.annotation.Nullable private Ingredient pattern = null;
  private boolean patternConsumed = false;

  /**
   * Creates a new builder of the given item
   * @param item  Item output
   * @return  Recipe
   */
  public static MoldingRecipeBuilder moldingTable(ItemLike item) {
    return molding(ItemOutput.fromItem(item), TinkerSmeltery.moldingTableSerializer);
  }

  /**
   * Creates a new builder of the given item
   * @param item  Item output
   * @return  Recipe
   */
  public static MoldingRecipeBuilder moldingBasin(ItemLike item) {
    return molding(ItemOutput.fromItem(item), TinkerSmeltery.moldingBasinSerializer);
  }

  /* Inputs */

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(Ingredient ingredient) {
    this.material = ingredient;
    return this;
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(ItemLike item) {
    return setMaterial(Ingredient.of(item));
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(TagKey<Item> tag) {
    return setMaterial(modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(tag));
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(Ingredient ingredient, boolean consumed) {
    this.pattern = ingredient;
    this.patternConsumed = consumed;
    return this;
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(ItemLike item, boolean consumed) {
    return setPattern(Ingredient.of(item), consumed);
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(TagKey<Item> tag, boolean consumed) {
    return setPattern(modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(tag), consumed);
  }


  /* Building */

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(output.get().getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (material == null) {
      throw new IllegalStateException("Missing material for molding recipe");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "molding");
    consumer.accept(new LoadableFinishedRecipe<>(id, new MoldingRecipe(serializer, id, material, pattern, patternConsumed, output), MoldingRecipe.LOADER, advancementId));
  }

  private class Finished extends AbstractFinishedRecipe {
    public Finished(Identifier ID, @Nullable Identifier advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("material", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, material).getOrThrow(IllegalArgumentException::new));
      if (pattern != null) {
        json.add("pattern", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, pattern).getOrThrow(IllegalArgumentException::new));
        if (patternConsumed) {
          json.addProperty("pattern_consumed", true);
        }
      }
      json.add("result", output.serialize(false));
    }

    @Override
    public RecipeSerializer<?> getType() {
      return serializer.serializer();
    }
  }
}
