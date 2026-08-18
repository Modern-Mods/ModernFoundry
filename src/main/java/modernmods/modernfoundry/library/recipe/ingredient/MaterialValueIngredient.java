package modernmods.modernfoundry.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicate;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicateField;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipe;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipeCache;
import modernmods.modernfoundry.shared.TinkerMaterials;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ingredient matching material items with the given value. Typically, matches ingots or blocks
 */
@Getter
@RequiredArgsConstructor
public class MaterialValueIngredient implements ICustomIngredient {
  private final IJsonPredicate<MaterialVariantId> material;
  private final float minValue;
  private final float maxValue;
  private java.util.List<Holder<Item>> items;

  /** Creates an ingredient matching a range of values */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, minValue, maxValue).toVanilla();
  }

  /** Creates an ingredient matching an exact value */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  /** Checks the given material recipe against our filters */
  public boolean test(MaterialRecipe material) {
    float value = material.getValue() / (float) material.getNeeded();
    return minValue <= value && value <= maxValue && this.material.matches(material.getMaterial().getVariant());
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null) {
      return false;
    }
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  @Override
  public Stream<Holder<Item>> items() {
    if (items == null) {
      items = MaterialRecipeCache.getAllRecipes().stream()
        .filter(this::test)
        .flatMap(material -> material.getIngredient().items())
        .toList();
    }
    return items.stream();
  }

  @Override
  public boolean isSimple() {
    return true;
  }


  /* Helpers for ShapedMaterialRecipe */

  /** Checks if this ingredient fully contains the range of the other */
  private boolean contains(MaterialValueIngredient other) {
    return this.minValue <= other.minValue && other.maxValue <= this.maxValue;
  }

  /** Creates an ingredient that matches anything either of the two ingredients matches */
  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;

    // if we have the same predicate, we can possibly skip creating a new instance
    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Math.min(this.minValue, other.minValue), Math.max(this.maxValue, other.maxValue));
  }

  /** Gets the material matching this recipe */
  @Nullable
  public MaterialVariantId getMaterial(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }


  /* JSON */

  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", Serializer.ID.toString());
    Serializer.MATERIAL_FIELD.serialize(this, json);
    if (minValue == maxValue) {
      json.addProperty("value", minValue);
    } else {
      JsonObject value = new JsonObject();
      if (minValue > 0) {
        value.addProperty("min", minValue);
      }
      if (Float.isFinite(maxValue)) {
        value.addProperty("max", maxValue);
      }
      json.add("value", value);
    }
    return json;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerMaterials.materialValueIngredient.get();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof MaterialValueIngredient that && Float.compare(minValue, that.minValue) == 0 && Float.compare(maxValue, that.maxValue) == 0 && material.equals(that.material);
  }

  @Override
  public int hashCode() {
    return Objects.hash(material, minValue, maxValue);
  }


  /** Serializer instance */
  public enum Serializer {
    INSTANCE;
    public static final Identifier ID = TConstruct.getResource("material_value");
    private static final LoadableField<IJsonPredicate<MaterialVariantId>, MaterialValueIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);

    /** Parses the ingredient from the legacy JSON format */
    private static MaterialValueIngredient parseJson(JsonObject json) {
      float minValue, maxValue;
      JsonElement value = json.get("value");
      if (value.isJsonPrimitive()) {
        minValue = maxValue = value.getAsJsonPrimitive().getAsFloat();
      } else {
        JsonObject object = GsonHelper.convertToJsonObject(value, "value");
        minValue = GsonHelper.getAsFloat(object, "min", 0);
        maxValue = GsonHelper.getAsFloat(object, "max", Float.POSITIVE_INFINITY);
      }
      return new MaterialValueIngredient(MATERIAL_FIELD.get(json), minValue, maxValue);
    }

    private static final MapCodec<MaterialValueIngredient> MAP_CODEC = new MapCodec<>() {
      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
      }

      @Override
      public <T> DataResult<MaterialValueIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> json.add(
          ops.convertTo(JsonOps.INSTANCE, pair.getFirst()).getAsString(),
          ops.convertTo(JsonOps.INSTANCE, pair.getSecond())));
        try {
          return DataResult.success(parseJson(json));
        } catch (RuntimeException e) {
          return DataResult.error(() -> "Failed to parse material_value ingredient: " + e.getMessage());
        }
      }

      @Override
      public <T> RecordBuilder<T> encode(MaterialValueIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        for (Map.Entry<String, JsonElement> entry : input.toJson().getAsJsonObject().entrySet()) {
          if (!"type".equals(entry.getKey())) {
            prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
          }
        }
        return prefix;
      }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, MaterialValueIngredient> STREAM_CODEC = StreamCodec.of(
      (buffer, ingredient) -> {
        MATERIAL_FIELD.encode(buffer, ingredient);
        buffer.writeFloat(ingredient.minValue);
        buffer.writeFloat(ingredient.maxValue);
      },
      buffer -> new MaterialValueIngredient(
        MATERIAL_FIELD.decode(buffer),
        buffer.readFloat(),
        buffer.readFloat()));

    public MapCodec<MaterialValueIngredient> codec() {
      return MAP_CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, MaterialValueIngredient> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
