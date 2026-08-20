package modernmods.modernfoundry.tools.yoyo.control;

import modernmods.modernfoundry.tools.yoyo.YoyoContext;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.util.Mth;
import org.joml.Vector3d;

public final class Mover {
  private final Vector3d scratch = new Vector3d();

  public void tick(YoyoEntity yoyo, YoyoContext context) {
    applyDrag(context);
    applyNearDrag(context);
    applyTargetingForce(context);
    constrainWithinDistance(context);
  }

  private void applyDrag(YoyoContext context) {
    double velocitySquared = context.velocity.lengthSquared();
    if (Mth.equal(velocitySquared, 0.0)) {
      return;
    }
    double force = velocitySquared / 2.0 * (0.25 * 0.25) * YoyoContext.DRAG_COEFFICIENT;
    double acceleration = Math.min(force * YoyoContext.INV_MASS, Math.sqrt(velocitySquared));
    scratch.set(context.velocity).normalize(-acceleration).add(context.velocity, context.velocity);
  }

  private void applyNearDrag(YoyoContext context) {
    double velocity = context.velocity.length();
    if (Mth.equal(velocity, 0.0)) {
      return;
    }
    scratch.set(context.targetPos).sub(context.ourPos);
    double distanceSquared = scratch.lengthSquared();
    double force = velocity * Math.pow(100.0, -distanceSquared * YoyoContext.INV_MASS);
    double acceleration = force / Math.max(YoyoContext.MASS, 1.0);
    scratch.set(context.velocity).normalize(-acceleration).add(context.velocity, context.velocity);
  }

  private void applyTargetingForce(YoyoContext context) {
    scratch.set(context.targetPos).sub(context.ourPos);
    double distanceSquared = scratch.lengthSquared();
    double distance = Math.sqrt(distanceSquared);
    double force = 1.0 - Math.pow(10.0, -(distanceSquared * distanceSquared)) + distance / 4.0;
    scratch.normalize(force * YoyoContext.INV_MASS).add(context.velocity, context.velocity);
  }

  private void constrainWithinDistance(YoyoContext context) {
    double nextDistance = scratch.set(context.ourPos).add(context.velocity).distance(context.tailPos);
    if (nextDistance > YoyoContext.TARGET_DISTANCE) {
      scratch.sub(context.tailPos).normalize(YoyoContext.TARGET_DISTANCE - nextDistance).add(context.velocity, context.velocity);
    }
  }
}
