package modernmods.modernfoundry.library.tools.layout;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.util.JsonHelper;
import modernmods.modernfoundry.library.recipe.partbuilder.Pattern;
import modernmods.modernfoundry.library.utils.TagUtil;

import javax.annotation.Nullable;

/** Data holder for a button icon, currently supports item stack icons and pattern icons */
public abstract class LayoutIcon {
  /** JSON serializer for a layout button icon */
  public static final Serializer SERIALIZER = new Serializer();

  /** Empty icon, used primarily as a fallback */
  public static final LayoutIcon EMPTY = new LayoutIcon() {
    @Nullable
    @Override
    public <T> T getValue(Class<T> clazz) {
      return null;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
      buffer.writeEnum(Type.EMPTY);
    }

    @Override
    public JsonObject toJson() {
      return new JsonObject();
    }
  };

  /** Creates a stack icon */
  public static LayoutIcon ofItem(ItemStack stack) {
    return new ItemStackIcon(stack);
  }

  /** Creates an icon from a pattern */
  public static LayoutIcon ofPattern(Pattern pattern) {
    return new PatternIcon(pattern);
  }

  /**
   * Creates a stack icon from raw item + NBT, without constructing an {@link ItemStack}. Used by datagen where item
   * DataComponents are not yet bound (so {@code new ItemStack(item)} throws). Only {@link #toJson()} is supported;
   * at load time the JSON deserializes back into a normal item-stack icon.
   */
  public static LayoutIcon ofRawItem(Item item, @Nullable CompoundTag nbt) {
    return new ItemStackIcon(item, nbt);
  }

  /** Gets the value of this icon, done this way to separate the drawing logic out */
  @Nullable
  public abstract <T> T getValue(Class<T> clazz);

  /** Reads the button icon from the buffer */
  public static LayoutIcon read(FriendlyByteBuf buffer) {
    Type type = buffer.readEnum(Type.class);
    switch (type) {
      case EMPTY: return EMPTY;
      case ITEM: {
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf)buffer);
        return new ItemStackIcon(stack);
      }
      case PATTERN: {
        Pattern pattern = new Pattern(buffer.readIdentifier());
        return new PatternIcon(pattern);
      }
    }
    throw new DecoderException("Invalid LayoutButtonIcon " + type);
  }

  /** Writes this to the packet buffer */
  public abstract void write(FriendlyByteBuf buffer);

  /** Writes this object to json */
  public abstract JsonObject toJson();

  /** Icon drawing an item stack */
  @VisibleForTesting
  protected static class ItemStackIcon extends LayoutIcon {
    private final Item item;
    @Nullable
    private final CompoundTag nbt;
    /** Lazily built stack: {@code new ItemStack(item)} reads item DataComponents, which are unbound during the datapack
     * reload prepare phase and at datagen ("Components not bound yet"). Building it on first access (client render time)
     * keeps deserialization safe; the dedicated server never renders the icon so it never builds. */
    @Nullable
    private ItemStack stack;

    ItemStackIcon(ItemStack stack) {
      this.item = stack.getItem();
      this.nbt = TagUtil.getTag(stack);
      this.stack = stack;
    }

    ItemStackIcon(Item item, @Nullable CompoundTag nbt) {
      this.item = item;
      this.nbt = nbt;
    }

    private ItemStack getStack() {
      if (stack == null) {
        ItemStack built = new ItemStack(item);
        if (nbt != null) {
          TagUtil.setTag(built, nbt.copy());
        }
        stack = built;
      }
      return stack;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(Class<T> clazz) {
      if (clazz == ItemStack.class) {
        return (T) getStack();
      }
      return null;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
      buffer.writeEnum(Type.ITEM);
      ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf)buffer, getStack());
    }

    @Override
    public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.addProperty("item", BuiltInRegistries.ITEM.getKey(item).toString());
      if (nbt != null && !nbt.isEmpty()) {
        json.addProperty("nbt", nbt.toString());
      }
      return json;
    }
  }

  /** Icon drawing a static patttern sprite */
  @RequiredArgsConstructor @VisibleForTesting
  protected static class PatternIcon extends LayoutIcon {
    private final Pattern pattern;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(Class<T> clazz) {
      if (clazz == Pattern.class) {
        return (T) pattern;
      }
      return null;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
      buffer.writeEnum(Type.PATTERN);
      buffer.writeIdentifier(pattern.getIdentifier());
    }

    @Override
    public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.addProperty("pattern", pattern.toString());
      return json;
    }
  }

  /** enum of icon types for serialization */
  private enum Type {
    EMPTY,
    ITEM,
    PATTERN
  }

  /** Serializer class */
  protected static class Serializer implements JsonSerializer<LayoutIcon>, JsonDeserializer<LayoutIcon> {
    @Override
    public LayoutIcon deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject object = GsonHelper.convertToJsonObject(json, "button_icon");
      if (object.has("pattern")) {
        Pattern pattern = new Pattern(JsonHelper.getResourceLocation(object, "pattern"));
        return new PatternIcon(pattern);
      }
      if (object.has("item")) {
        Identifier itemId = JsonHelper.getResourceLocation(object, "item");
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + itemId + "'"));
        // build the icon lazily from raw item + NBT: constructing the ItemStack here reads item DataComponents, which are
        // unbound during the datapack reload prepare phase ("Components not bound yet"). ItemStackIcon builds it on demand.
        CompoundTag nbt = null;
        if (object.has("nbt")) {
          try {
            nbt = TagParser.parseCompoundFully(GsonHelper.getAsString(object, "nbt"));
          } catch (Exception e) {
            throw new JsonSyntaxException("Invalid item NBT", e);
          }
        }
        return new ItemStackIcon(item, nbt);
      }
      // not sure why this would be needed, but might as well
      if (object.entrySet().isEmpty()) {
        return EMPTY;
      }
      throw new JsonSyntaxException("LayoutButtonIcon must have either pattern or item");
    }

    @Override
    public JsonElement serialize(LayoutIcon icon, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
      return icon.toJson();
    }
  }
}
