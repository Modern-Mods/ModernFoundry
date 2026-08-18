package modernmods.modernfoundry.library.recipe.ingredient;

import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.library.utils.JsonUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/** Ingredient matching an item with no container item, used to ensure NBT fluid items are empty */
public class NoContainerIngredient extends NestedIngredient {
  public static final Identifier ID = TConstruct.getResource("no_container");

  protected NoContainerIngredient(Ingredient nested) {
    super(nested);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && super.test(stack) && stack.getItem().getCraftingRemainder(stack) == null;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  /**
   * Registry-aware JSON ops, built lazily. Serializing a tag {@link Ingredient} needs a {@link net.minecraft.resources.RegistryOps}
   * so {@code HolderSetCodec} writes the tag by name (via {@code unwrapKey}); plain {@link JsonOps} instead iterates the holder
   * set contents ({@code encodeWithoutRegistry}), which throws "Missing tag" at datagen time (tags are not bound then).
   */
  private static DynamicOps<JsonElement> jsonOps;
  private static DynamicOps<JsonElement> jsonOps() {
    if (jsonOps == null) {
      jsonOps = net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }
    return jsonOps;
  }

  public JsonElement toJson() {
    JsonElement nestedElement = Ingredient.CODEC.encodeStart(jsonOps(), nested).getOrThrow(IllegalArgumentException::new);
    // if we are a vanilla ingredient, and not an array ingredient, serialize into the ingredient directly
    if (!nested.isCustom() && nestedElement.isJsonObject()) {
      JsonObject nestedObject = nestedElement.getAsJsonObject();
      nestedObject.addProperty("type", ID.toString());
      return nestedObject;
    }
    // if we have an array or a type, then serialize nested
    JsonObject json = JsonUtils.withType(ID);
    json.add("match", nestedElement);
    return json;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerCommons.noContainerIngredient.get();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof NoContainerIngredient that && nested.equals(that.nested);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nested);
  }

  public enum Serializer {
    INSTANCE;

    /** Parses the ingredient from the legacy JSON format (supports both the inline vanilla form and the "match" wrapper) */
    private static NoContainerIngredient parseJson(JsonObject json) {
      // if we have match, parse as a nested object. Without match, just parse the object as vanilla
      // route through IngredientLoadable.convert, which accepts a bare item/tag string, the legacy {"item"}/{"tag"}
      // object forms, arrays, and custom ingredients -- the raw Ingredient.CODEC (HolderSet-based) rejects a bare item id
      Ingredient ingredient;
      if (json.has("match")) {
        ingredient = modernmods.mantle.data.loadable.common.IngredientLoadable.DISALLOW_EMPTY.convert(json.get("match"), "match", modernmods.mantle.util.typed.TypedMap.empty());
      } else {
        JsonObject copy = json.deepCopy();
        copy.remove("type");
        ingredient = modernmods.mantle.data.loadable.common.IngredientLoadable.DISALLOW_EMPTY.convert(copy, "match", modernmods.mantle.util.typed.TypedMap.empty());
      }
      return new NoContainerIngredient(ingredient);
    }

    private static final MapCodec<NoContainerIngredient> MAP_CODEC = new MapCodec<>() {
      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
      }

      @Override
      public <T> DataResult<NoContainerIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> json.add(
          ops.convertTo(JsonOps.INSTANCE, pair.getFirst()).getAsString(),
          ops.convertTo(JsonOps.INSTANCE, pair.getSecond())));
        try {
          return DataResult.success(parseJson(json));
        } catch (RuntimeException e) {
          return DataResult.error(() -> "Failed to parse no_container ingredient: " + e.getMessage());
        }
      }

      @Override
      public <T> RecordBuilder<T> encode(NoContainerIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        for (Map.Entry<String, JsonElement> entry : input.toJson().getAsJsonObject().entrySet()) {
          if (!"type".equals(entry.getKey())) {
            prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
          }
        }
        return prefix;
      }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, NoContainerIngredient> STREAM_CODEC = StreamCodec.of(
      (buffer, ingredient) -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient.nested),
      buffer -> new NoContainerIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)));

    public MapCodec<NoContainerIngredient> codec() {
      return MAP_CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, NoContainerIngredient> streamCodec() {
      return STREAM_CODEC;
    }
  }


  /* Static constructors */

  /** Creates an instance from the given nested ingredient */
  public static Ingredient of(Ingredient ingredient) {
    return new NoContainerIngredient(ingredient).toVanilla();
  }

  /** Creates an instance from the given items */
  public static Ingredient of(ItemLike... items) {
    return of(Ingredient.of(items));
  }

  /** Creates an instance from the given stacks */
  public static Ingredient of(ItemStack... stacks) {
    // 26.1.2 Ingredient.of no longer accepts ItemStacks (ingredients are item-based); use the stacks' items
    return of(Ingredient.of(java.util.Arrays.stream(stacks).map(ItemStack::getItem).toArray(Item[]::new)));
  }

  /** Creates an instance from the given tag */
  public static Ingredient of(TagKey<Item> tag) {
    return of(modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(tag));
  }
}
