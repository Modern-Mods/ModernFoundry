package modernmods.modernfoundry.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.util.RegistryHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.shared.TinkerCommons;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/** Item ingredient matching items with a block form in the given tag */
@RequiredArgsConstructor
public class BlockTagIngredient implements ICustomIngredient {
  private final TagKey<Block> tag;
  @Nullable
  private Set<Item> matchingItems;
  @Nullable
  private java.util.List<net.minecraft.core.Holder<Item>> items;

  public static Ingredient of(TagKey<Block> tag) {
    return new BlockTagIngredient(tag).toVanilla();
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && getMatchingItems().contains(stack.getItem());
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  /** Gets the ordered matching items set */
  private Set<Item> getMatchingItems() {
    if (matchingItems == null) {
      matchingItems = RegistryHelper.getTagValueStream(BuiltInRegistries.BLOCK, tag)
                                    .map(Block::asItem)
                                    .filter(item -> item != Items.AIR)
                                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
    return matchingItems;
  }

  @Override
  public Stream<net.minecraft.core.Holder<Item>> items() {
    if (items == null) {
      items = getMatchingItems().stream().map(Item::builtInRegistryHolder).collect(java.util.stream.Collectors.toList());
      // 26.1.2 items() returns item holders; empty tags fall back to a plain barrier holder placeholder
      if (items.isEmpty()) {
        items = java.util.List.of(Items.BARRIER.builtInRegistryHolder());
      }
    }
    return items.stream();
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerCommons.blockTagIngredient.get();
  }

  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", Serializer.ID.toString());
    json.add("tag", Loadables.BLOCK_TAG.serialize(tag));
    return json;
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof BlockTagIngredient that && tag.equals(that.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag);
  }

  /** Serializer instance */
  public enum Serializer {
    INSTANCE;

    public static final Identifier ID = TConstruct.getResource("block_tag");

    /** Parses the ingredient from the legacy JSON format */
    private static BlockTagIngredient parseJson(JsonObject json) {
      return new BlockTagIngredient(Loadables.BLOCK_TAG.getIfPresent(json, "tag"));
    }

    private static final MapCodec<BlockTagIngredient> MAP_CODEC = new MapCodec<>() {
      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
      }

      @Override
      public <T> DataResult<BlockTagIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> json.add(
          ops.convertTo(JsonOps.INSTANCE, pair.getFirst()).getAsString(),
          ops.convertTo(JsonOps.INSTANCE, pair.getSecond())));
        try {
          return DataResult.success(parseJson(json));
        } catch (RuntimeException e) {
          return DataResult.error(() -> "Failed to parse block_tag ingredient: " + e.getMessage());
        }
      }

      @Override
      public <T> RecordBuilder<T> encode(BlockTagIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        for (Map.Entry<String, JsonElement> entry : input.toJson().getAsJsonObject().entrySet()) {
          if (!"type".equals(entry.getKey())) {
            prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
          }
        }
        return prefix;
      }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> STREAM_CODEC = StreamCodec.of(
      (buffer, ingredient) -> Loadables.BLOCK_TAG.encode(buffer, ingredient.tag),
      buffer -> new BlockTagIngredient(Loadables.BLOCK_TAG.decode(buffer)));

    public MapCodec<BlockTagIngredient> codec() {
      return MAP_CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
