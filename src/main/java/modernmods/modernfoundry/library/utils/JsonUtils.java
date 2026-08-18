package modernmods.modernfoundry.library.utils;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import modernmods.mantle.network.packet.ISimplePacket;
import modernmods.mantle.util.JsonHelper;
import modernmods.modernfoundry.common.network.TinkerNetwork;

/** Helpers for a few JSON related tasks */
public class JsonUtils {
  private JsonUtils() {}

  /** Called when the player logs in to send packets */
  public static void syncPackets(OnDatapackSyncEvent event, ISimplePacket... packets) {
    JsonHelper.syncPackets(event, TinkerNetwork.getInstance(), packets);
  }

  /** Creates a JSON object with the given key set to a resource location */
  public static JsonObject withLocation(String key, Identifier value) {
    JsonObject json = new JsonObject();
    json.addProperty(key, value.toString());
    return json;
  }

  /** Creates a JSON object with the given type set, makes using {@link modernmods.mantle.data.gson.GenericRegisteredSerializer} easier */
  public static JsonObject withType(Identifier type) {
    return withLocation("type", type);
  }
}
