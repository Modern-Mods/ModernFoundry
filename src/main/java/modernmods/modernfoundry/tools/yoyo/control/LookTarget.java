package modernmods.modernfoundry.tools.yoyo.control;

import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import modernmods.modernfoundry.tools.yoyo.YoyoTarget;
import modernmods.modernfoundry.tools.yoyo.YoyoUtil;
import modernmods.modernfoundry.tools.yoyo.YoyoContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3d;

public final class LookTarget implements YoyoTarget {
  private final Vector3d scratch = new Vector3d();

  @Override
  public void updateTarget(YoyoEntity yoyo, Entity owner, YoyoContext context) {
    YoyoUtil.storeEntityViewVec(scratch, owner, 0.0F).mul(YoyoContext.TARGET_DISTANCE);
    AABB entitySearchBounds = owner.getBoundingBox().expandTowards(scratch.x, scratch.y, scratch.z).inflate(1.0);
    context.targetPos.set(context.tailPos).add(scratch);

    var level = yoyo.level();
    HitResult blockHit = level.clip(new ClipContext(YoyoUtil.asVec3(context.tailPos), YoyoUtil.asVec3(context.targetPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
    if (blockHit.getType() != HitResult.Type.MISS) {
      YoyoUtil.set(context.targetPos, blockHit.getLocation());
    }

    double closestDistanceSqr = context.targetPos.distanceSquared(context.tailPos);
    for (Entity entity : level.getEntities(owner, entitySearchBounds, yoyo.getCollisionPredicate())) {
      AABB checkBounds = entity.getBoundingBox().inflate(0.15);
      boolean hit = YoyoUtil.clip(checkBounds, context.tailPos, context.targetPos, scratch);
      if (checkBounds.contains(context.tailPos.x, context.tailPos.y, context.tailPos.z)) {
        if (closestDistanceSqr >= 0.0) {
          if (hit) {
            context.targetPos.set(scratch);
          } else {
            context.targetPos.set(context.tailPos);
            return;
          }
          closestDistanceSqr = 0.0;
        }
      } else if (hit) {
        double distanceSqr = context.tailPos.distanceSquared(scratch);
        if (distanceSqr < closestDistanceSqr) {
          context.targetPos.set(scratch);
          closestDistanceSqr = distanceSqr;
        }
      }
    }
  }
}
