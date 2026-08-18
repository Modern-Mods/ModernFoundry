package modernmods.modernfoundry.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.item.IModifiable;
import modernmods.modernfoundry.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/** Ingredient that only matches tools with a specific hook */
public class ToolHookIngredient implements ICustomIngredient {
  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;
  @Nullable
  private List<Holder<Item>> items;

  protected ToolHookIngredient(TagKey<Item> tag, ModuleHook<?> hook) {
    this.tag = tag;
    this.hook = hook;
  }

  public static Ingredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook).toVanilla();
  }

  public static Ingredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && stack.is(tag) && stack.getItem() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public Stream<Holder<Item>> items() {
    if (items == null) {
      List<Holder<Item>> list = new ArrayList<>();
      for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
        if (holder.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
          list.add(holder);
        }
      }
      // 26.1.2 ICustomIngredient#items() returns item holders (display stacks are built by the framework),
      // so the former named-barrier placeholder for empty tags becomes a plain barrier holder.
      if (list.isEmpty()) {
        list.add(net.minecraft.world.item.Items.BARRIER.builtInRegistryHolder());
      }
      items = list;
    }
    return items.stream();
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerTools.toolHookIngredient.get();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", Serializer.ID.toString());
    json.addProperty("tag", tag.location().toString());
    json.addProperty("hook", hook.getId().toString());
    return json;
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof ToolHookIngredient that && tag.equals(that.tag) && hook.equals(that.hook);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag, hook);
  }

  /** Serializer instance */
  public enum Serializer {
    INSTANCE;

    public static final Identifier ID = TConstruct.getResource("tool_hook");

    /** Parses the ingredient from the legacy JSON format */
    private static ToolHookIngredient parseJson(JsonObject json) {
      return new ToolHookIngredient(
        Loadables.ITEM_TAG.getOrDefault(json, "tag", TinkerTags.Items.MODIFIABLE),
        ToolHooks.LOADER.getIfPresent(json, "hook")
      );
    }

    private static final MapCodec<ToolHookIngredient> MAP_CODEC = new MapCodec<>() {
      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
      }

      @Override
      public <T> DataResult<ToolHookIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> json.add(
          ops.convertTo(JsonOps.INSTANCE, pair.getFirst()).getAsString(),
          ops.convertTo(JsonOps.INSTANCE, pair.getSecond())));
        try {
          return DataResult.success(parseJson(json));
        } catch (RuntimeException e) {
          return DataResult.error(() -> "Failed to parse tool_hook ingredient: " + e.getMessage());
        }
      }

      @Override
      public <T> RecordBuilder<T> encode(ToolHookIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        for (Map.Entry<String, JsonElement> entry : input.toJson().entrySet()) {
          if (!"type".equals(entry.getKey())) {
            prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
          }
        }
        return prefix;
      }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, ToolHookIngredient> STREAM_CODEC = StreamCodec.of(
      (buffer, ingredient) -> {
        Loadables.ITEM_TAG.encode(buffer, ingredient.tag);
        ToolHooks.LOADER.encode(buffer, ingredient.hook);
      },
      buffer -> new ToolHookIngredient(
        Loadables.ITEM_TAG.decode(buffer),
        ToolHooks.LOADER.decode(buffer)));

    public MapCodec<ToolHookIngredient> codec() {
      return MAP_CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, ToolHookIngredient> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
