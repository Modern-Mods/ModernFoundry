package modernmods.modernfoundry.library.client.model.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import modernmods.mantle.client.model.util.ColoredBlockModel;

/**
 * Model that used to swap fluid textures with the fluid from model data.
 * <p>
 * 26.1 static-shell: the dynamic fluid (and NBT retexturing) logic was removed. The dynamic fluid is now drawn by the
 * block-entity renderer (e.g. {@code TankBlockEntityRenderer}), so the model is just the static coloured shell. The
 * {@code "fluids"}/{@code "retextured"} keys, if present in the JSON, are ignored.
 */
public class FluidTextureModel extends ColoredBlockModel {
  /** Loader instance */
  public static final UnbakedModelLoader<FluidTextureModel> LOADER = FluidTextureModel::deserialize;

  private FluidTextureModel(ColoredBlockModel model) {
    super(model, model.getColorData());
  }

  /** Deserializes this model from JSON */
  public static FluidTextureModel deserialize(JsonObject json, JsonDeserializationContext context) {
    // static shell: only the coloured geometry remains; dynamic fluid now drawn in the block-entity renderer
    return new FluidTextureModel(ColoredBlockModel.deserialize(json, context));
  }
}
