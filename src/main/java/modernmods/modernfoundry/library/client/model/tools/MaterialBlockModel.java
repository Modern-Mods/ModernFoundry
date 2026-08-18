package modernmods.modernfoundry.library.client.model.tools;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import modernmods.mantle.client.model.util.SimpleBlockModel;

/**
 * Model that handled dynamic materials using the block model elements style (tool/part/anvil block variants).
 * <p>
 * 26.1 static shell: the per-material dynamic retexture (swapping element textures for the material's texture, driven by
 * NBT/blockstate through the removed BakedModel/ItemOverrides pipeline) was removed. This bakes the base block geometry
 * only; the material-specific textures must be re-expressed via the new item/block model texture-variant or tint system.
 * @see MaterialModel
 * @see ToolModel
 */
public class MaterialBlockModel extends SimpleBlockModel {
  /** Shared loader instance */
  public static final UnbakedModelLoader<MaterialBlockModel> LOADER = MaterialBlockModel::deserialize;

  private MaterialBlockModel(SimpleBlockModel model) {
    super(model);
  }

  /** Loads a material block model from JSON */
  public static MaterialBlockModel deserialize(JsonObject json, JsonDeserializationContext context) {
    // static shell: bake the base geometry; dynamic per-material retexture removed (material/retextured/parts keys ignored)
    return new MaterialBlockModel(SimpleBlockModel.deserialize(json, context));
  }
}
