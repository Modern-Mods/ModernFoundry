package modernmods.modernfoundry.library.recipe.ingredient;

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
import net.minecraft.resources.ResourceLocation;
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
  public static final ResourceLocation ID = TConstruct.getResource("no_container");

  protected NoContainerIngredient(Ingredient nested) {
    super(nested);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && super.test(stack) && !stack.hasCraftingRemainingItem();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  public JsonElement toJson() {
    JsonElement nestedElement = Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, nested).getOrThrow(IllegalArgumentException::new);
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
      Ingredient ingredient;
      if (json.has("match")) {
        ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, json.get("match")).getOrThrow(IllegalArgumentException::new);
      } else {
        JsonObject copy = json.deepCopy();
        copy.remove("type");
        ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, copy).getOrThrow(IllegalArgumentException::new);
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
    return of(Ingredient.of(stacks));
  }

  /** Creates an instance from the given tag */
  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }
}
