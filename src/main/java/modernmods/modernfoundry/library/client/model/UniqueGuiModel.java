package modernmods.modernfoundry.library.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import modernmods.mantle.client.model.util.SimpleBlockModel;

/**
 * Model providing a variant for the GUI.
 * <p>
 * 26.1 removed the per-display-context baked-model swap (the old {@code BakedModelWrapper#applyTransform} that returned
 * a different baked model for {@link net.minecraft.world.item.ItemDisplayContext#GUI}). Display transforms are now
 * data-driven through the model's {@code display} block, so this is a static shell that bakes the main model geometry.
 * The separate {@code "gui"} sub-model is ignored; a distinct GUI appearance should be expressed via GUI display
 * transforms (or a dedicated item model) in JSON.
 */
public class UniqueGuiModel extends SimpleBlockModel {
  /** Shared loader instance */
  public static final UnbakedModelLoader<UniqueGuiModel> LOADER = UniqueGuiModel::deserialize;

  private UniqueGuiModel(SimpleBlockModel model) {
    super(model);
  }

  /** Loader for this model */
  public static UniqueGuiModel deserialize(JsonObject json, JsonDeserializationContext context) {
    // static shell: bake the main model; the "gui" variant swap is gone in 26.1 (display transforms are data-driven)
    return new UniqueGuiModel(SimpleBlockModel.deserialize(json, context));
  }
}
