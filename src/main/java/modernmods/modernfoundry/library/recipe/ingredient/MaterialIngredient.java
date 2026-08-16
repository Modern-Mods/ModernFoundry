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
import modernmods.hilt.data.loadable.field.LoadableField;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicate;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicateField;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipeCache;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.shared.TinkerMaterials;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Extension of the vanilla ingredient to display materials on items and support matching by materials
 */
public class MaterialIngredient extends NestedIngredient {
  private final IJsonPredicate<MaterialVariantId> material;
  @Nullable
  private ItemStack[] materialStacks;
  protected MaterialIngredient(Ingredient nested, IJsonPredicate<MaterialVariantId> material) {
    super(nested);
    this.material = material;
  }

  /** @deprecated use {@link #MaterialIngredient(Ingredient, IJsonPredicate)} */
  @Deprecated(forRemoval = true)
  protected MaterialIngredient(Ingredient nested, MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    this(nested, makePredicate(material, tag));
  }

  /** Converts the legacy material and tag into a predicate */
  private static IJsonPredicate<MaterialVariantId> makePredicate(MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    // UNKNOWN is the legacy way to express any material
    IJsonPredicate<MaterialVariantId> predicate = material.equals(IMaterial.UNKNOWN.getIdentifier()) ? MaterialPredicate.ANY : MaterialPredicate.variant(material);
    if (tag != null) {
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(tag);
      if (predicate == MaterialPredicate.ANY) {
        predicate = tagPredicate;
      } else {
        predicate = MaterialPredicate.and(predicate, tagPredicate);
      }
    }
    return predicate;
  }

  /** Creates an ingredient matching the given materials */
  public static Ingredient of(Ingredient ingredient, IJsonPredicate<MaterialVariantId> material) {
    return new MaterialIngredient(ingredient, material).toVanilla();
  }

  /** Creates an ingredient matching the given materials */
  public static Ingredient of(ItemLike item, IJsonPredicate<MaterialVariantId> material) {
    return of(Ingredient.of(item), material);
  }

  /** Creates an ingredient matching a specific material */
  public static Ingredient of(Ingredient ingredient) {
    return new MaterialIngredient(ingredient, MaterialPredicate.ANY).toVanilla();
  }

  /** Creates an ingredient matching a single material */
  public static Ingredient of(Ingredient ingredient, MaterialVariantId material) {
    return of(ingredient, MaterialPredicate.variant(material));
  }

  /** Creates an ingredient matching a material tag */
  public static Ingredient of(Ingredient ingredient, TagKey<IMaterial> tag) {
    return of(ingredient, MaterialPredicate.tag(tag));
  }

  /**
   * Creates a new instance from an item with a fixed material
   * @param item      Material item
   * @param material  Material ID
   * @return  Material ingredient instance
   */
  public static Ingredient of(ItemLike item, MaterialVariantId material) {
    return of(Ingredient.of(item), material);
  }

  /**
   * Creates a new instance from an item with a tagged material
   * @param item      Material item
   * @param tag   Material tag
   * @return  Material ingredient instance
   */
  public static Ingredient of(ItemLike item, TagKey<IMaterial> tag) {
    return of(Ingredient.of(item), tag);
  }

  /**
   * Creates a new ingredient matching any material from items
   * @param item  Material item
   * @return  Material ingredient instance
   */
  public static Ingredient of(ItemLike item) {
    return of(Ingredient.of(item));
  }

  /**
   * Creates a new ingredient from a tag
   * @param tag       Tag instance
   * @param material  Material value
   * @return  Material with tag
   */
  public static Ingredient of(TagKey<Item> tag, MaterialVariantId material) {
    return of(Ingredient.of(tag), material);
  }

  /**
   * Creates a new ingredient matching any material from a tag
   * @param tag       Tag instance
   * @return  Material with tag
   */
  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    // check super first, should be faster
    if (stack == null || stack.isEmpty() || !super.test(stack)) {
      return false;
    }
    // no need to read material NBT if the material is the any predicate
    if (material != MaterialPredicate.ANY) {
      return material.matches(IMaterialItem.getMaterialFromStack(stack));
    }
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (materialStacks == null) {
      if (!MaterialRegistry.isFullyLoaded()) {
        return Arrays.stream(nested.getItems());
      }
      // no material? apply all materials for variants
      Stream<ItemStack> items = Arrays.stream(nested.getItems());
      // find all materials matching the filter; note this only shows craftable material variants
      items = items.flatMap(stack -> MaterialRecipeCache.getAllVariants().stream()
        .filter(material::matches)
        .map(mat -> IMaterialItem.withMaterial(stack, mat))
        .filter(TagUtil::hasTag));
      materialStacks = items.distinct().toArray(ItemStack[]::new);
    }
    return Arrays.stream(materialStacks);
  }

  public JsonElement toJson() {
    JsonElement parent = Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, nested).getOrThrow(IllegalArgumentException::new);
    JsonObject result;
    if (!nested.isCustom() && parent.isJsonObject()) {
      result = parent.getAsJsonObject();
    } else {
      result = new JsonObject();
      result.add("match", parent);
    }
    result.addProperty("type", Serializer.ID.toString());
    Serializer.MATERIAL_FIELD.serialize(this, result);
    return result;
  }

  @Override
  public boolean isSimple() {
    return material == MaterialPredicate.ANY;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerMaterials.materialIngredient.get();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof MaterialIngredient that && nested.equals(that.nested) && material.equals(that.material);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nested, material);
  }

  /** Serializer instance */
  public enum Serializer {
    INSTANCE;
    public static final ResourceLocation ID = TConstruct.getResource("material");
    private static final LoadableField<IJsonPredicate<MaterialVariantId>,MaterialIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);

    /** Parses the ingredient from the legacy JSON format (supports both the inline vanilla form and the "match" wrapper) */
    private static MaterialIngredient parseJson(JsonObject json) {
      // if we have match, parse as a nested object. Without match, just parse the object as vanilla
      Ingredient ingredient;
      if (json.has("match")) {
        ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, json.get("match")).getOrThrow(IllegalArgumentException::new);
      } else {
        JsonObject copy = json.deepCopy();
        copy.remove("type");
        ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, copy).getOrThrow(IllegalArgumentException::new);
      }
      IJsonPredicate<MaterialVariantId> material = MATERIAL_FIELD.get(json);
      // deprecated tag field
      if (json.has("tag")) {
        TConstruct.LOG.warn("Using deprecated tag field on material ingredient");
        IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(TinkerLoadables.MATERIAL_TAGS.getIfPresent(json, "tag"));
        if (material == MaterialPredicate.ANY) {
          material = tagPredicate;
        } else {
          material = MaterialPredicate.and(material, tagPredicate);
        }
      }
      return new MaterialIngredient(ingredient, material);
    }

    private static final MapCodec<MaterialIngredient> MAP_CODEC = new MapCodec<>() {
      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
      }

      @Override
      public <T> DataResult<MaterialIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> json.add(
          ops.convertTo(JsonOps.INSTANCE, pair.getFirst()).getAsString(),
          ops.convertTo(JsonOps.INSTANCE, pair.getSecond())));
        try {
          return DataResult.success(parseJson(json));
        } catch (RuntimeException e) {
          return DataResult.error(() -> "Failed to parse material ingredient: " + e.getMessage());
        }
      }

      @Override
      public <T> RecordBuilder<T> encode(MaterialIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        for (Map.Entry<String, JsonElement> entry : input.toJson().getAsJsonObject().entrySet()) {
          if (!"type".equals(entry.getKey())) {
            prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
          }
        }
        return prefix;
      }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, MaterialIngredient> STREAM_CODEC = StreamCodec.of(
      (buffer, ingredient) -> {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient.nested);
        MATERIAL_FIELD.encode(buffer, ingredient);
      },
      buffer -> new MaterialIngredient(
        Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
        MATERIAL_FIELD.decode(buffer)));

    public MapCodec<MaterialIngredient> codec() {
      return MAP_CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, MaterialIngredient> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
