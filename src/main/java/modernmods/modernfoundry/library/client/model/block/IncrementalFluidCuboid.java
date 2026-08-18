package modernmods.modernfoundry.library.client.model.block;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;
import modernmods.mantle.client.render.FluidCuboid;

import java.util.Map;

/**
 * Extension of {@link FluidCuboid} which supports being scaled by the fluid amount, used in tank models.
 * <p>
 * The scaled-geometry generation previously produced vanilla {@code BlockElement}/{@code BlockElementFace}/{@code BlockFaceUV}
 * model data, all of which were removed in the 26.1 block-model rewrite. This class currently carries the cuboid data
 * (bounds, faces and increment count); the per-amount geometry will be regenerated against the new baking API together with
 * the TankModel port that consumes it.
 */
@Getter
public class IncrementalFluidCuboid extends FluidCuboid {
  private final int increments;
  @SuppressWarnings("WeakerAccess")
  public IncrementalFluidCuboid(Vector3f from, Vector3f to, Map<Direction,FluidFace> faces, int increments) {
    super(from, to, faces);
    this.increments = increments;
  }

  /**
   * Computes the scaled upper/lower bound of the fluid for the given amount, keeping the height-scaling math available for
   * the eventual geometry rebuild.
   * @param amount  Fluid amount
   * @param gas     If true, renders upside down
   * @return  Scaled fluid bounds as {from, to}
   */
  @SuppressWarnings("WeakerAccess")
  public Vector3f[] getScaledBounds(int amount, boolean gas) {
    Vector3f from = new Vector3f(getFrom());
    Vector3f to = new Vector3f(getTo());
    // gas renders upside down
    float minY = from.y();
    float maxY = to.y();
    if (gas) {
      from.y = maxY + (amount * (minY - maxY) / increments);
    } else {
      to.y = minY + (amount * (maxY - minY) / increments);
    }
    return new Vector3f[] {from, to};
  }

  /**
   * Creates a new scalable fluid cuboid from JSON
   * @param json  Fluid JSON object
   * @return  Scalable fluid cuboid
   */
  public static IncrementalFluidCuboid fromJson(JsonObject json) {
    FluidCuboid base = FluidCuboid.LOADABLE.deserialize(json);
    int increments = GsonHelper.getAsInt(json, "increments");
    return new IncrementalFluidCuboid(base.getFrom(), base.getTo(), base.getFaces(), increments);
  }
}
