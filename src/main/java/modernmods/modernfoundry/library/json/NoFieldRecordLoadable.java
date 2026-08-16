package modernmods.modernfoundry.library.json;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.util.typed.TypedMap;

import java.util.function.Supplier;

/** Same as {@link modernmods.hilt.data.loadable.record.SingletonLoader}, but creates a new instance each time. Used to cache unique data in the object. */
public record NoFieldRecordLoadable<T>(Supplier<T> constructor) implements RecordLoadable<T> {
  @Override
  public T deserialize(JsonObject json, TypedMap context) {
    return constructor.get();
  }

  @Override
  public void serialize(T object, JsonObject json) {}

  @Override
  public T decode(FriendlyByteBuf buffer, TypedMap context) {
    return constructor.get();
  }

  @Override
  public void encode(FriendlyByteBuf buffer, T value) {}
}
