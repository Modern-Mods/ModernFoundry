package modernmods.modernfoundry.library.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.mantle.util.JsonHelper;

import javax.annotation.Nullable;

/** Represents a redirect in a material or modifier JSON */
public class JsonRedirect {
  private final Identifier id;
  @Nullable
  private final ICondition condition;

  public JsonRedirect(Identifier id, @Nullable ICondition condition) {
    this.id = id;
    this.condition = condition;
  }

  public Identifier getId() {
    return id;
  }

  @Nullable
  public ICondition getCondition() {
    return condition;
  }

  /** Serializes this to JSON */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("id", id.toString());
    if (condition != null) {
      json.add("condition", ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(JsonParseException::new));
    }
    return json;
  }

  /** Deserializes this to JSON */
  public static JsonRedirect fromJson(JsonObject json) {
    Identifier id = JsonHelper.getResourceLocation(json, "id");
    ICondition condition = null;
    if (json.has("condition")) {
      condition = ICondition.CODEC.parse(JsonOps.INSTANCE, json.get("condition")).getOrThrow(JsonParseException::new);
    }
    return new JsonRedirect(id, condition);
  }
}
