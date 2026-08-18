package modernmods.modernfoundry.library.client.model.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import modernmods.mantle.client.model.util.SimpleBlockModel;

/**
 * Model for a tank block.
 * <p>
 * Historically this baked a single scalable fluid that rendered either statically or in the TESR, plus a GUI variant
 * and an item fluid. In the 26.1 render rewrite the dynamic fluid is drawn by {@code TankBlockEntityRenderer} and item
 * fluids by the item model system, and the per-display-context baked-model swap was removed. This is now a static
 * shell that bakes the base block geometry; the {@code "fluid"}/{@code "gui"} keys are ignored (fluid rendering lives
 * in the block-entity renderer now).
 */
public class TankModel extends SimpleBlockModel {
  /** Shared loader instance */
  public static final UnbakedModelLoader<TankModel> LOADER = TankModel::deserialize;

  private TankModel(SimpleBlockModel model) {
    super(model);
  }

  /** Deserializes this model from JSON */
  public static TankModel deserialize(JsonObject json, JsonDeserializationContext context) {
    // static shell: bake the base block geometry; dynamic fluid now drawn in TankBlockEntityRenderer
    return new TankModel(SimpleBlockModel.deserialize(json, context));
  }
}
