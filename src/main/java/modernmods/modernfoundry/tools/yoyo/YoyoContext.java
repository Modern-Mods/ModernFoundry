package modernmods.modernfoundry.tools.yoyo;

import org.joml.Vector3d;

/** Mutable per-entity state used by the yoyo controller. */
public final class YoyoContext {
  public static final double TARGET_DISTANCE = 6.0;
  public static final double DRAG_COEFFICIENT = 15.0;
  public static final double MASS = 3.0;
  public static final double INV_MASS = 1.0 / MASS;

  public final Vector3d velocity = new Vector3d();
  public final Vector3d tailPos = new Vector3d();
  public final Vector3d targetPos = new Vector3d();
  public final Vector3d ourPos = new Vector3d();
}
