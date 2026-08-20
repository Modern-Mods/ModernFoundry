package modernmods.modernfoundry.tools.yoyo.control;

import modernmods.modernfoundry.tools.yoyo.YoyoBehavior;
import modernmods.modernfoundry.tools.yoyo.YoyoContext;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public final class MotionResolver {
  private static final double STEPS_PER_BLOCK = 64.0;
  private final Vector3d scratchA = new Vector3d();
  private final Vector3d scratchB = new Vector3d();
  private final Vector3d scratchC = new Vector3d();

  public void solve(YoyoEntity yoyo, YoyoContext context, List<YoyoBehavior> behaviors) {
    AABB collisionHitBox = yoyo.getBoundingBox();
    AABB entityHitBox = collisionHitBox.inflate(0.2);
    AABB searchBox = entityHitBox.minmax(entityHitBox.move(context.velocity.x, context.velocity.y, context.velocity.z)).inflate(1.0);
    var level = yoyo.level();
    List<Entity> entities = new ArrayList<>(level.getEntities(yoyo, searchBox, yoyo.getCollisionPredicate()));
    List<VoxelShape> collisionShapes = new ArrayList<>();
    level.getCollisions(yoyo, searchBox).forEach(collisionShapes::add);

    int steps = Mth.ceil(context.velocity.length() * STEPS_PER_BLOCK);
    scratchA.set(context.velocity).normalize(1.0 / STEPS_PER_BLOCK);
    scratchB.zero();
    for (int i = 0; i < steps; i++) {
      hitAndFilterEntities(yoyo, context, behaviors, entities, entityHitBox.move(scratchB.x, scratchB.y, scratchB.z));
      collideWithShapes(yoyo, context, behaviors, collisionShapes, collisionHitBox);
      scratchB.add(scratchA);
    }

    context.velocity.set(scratchB);
    yoyo.setCenterPos(scratchA.set(context.ourPos).add(scratchB));
    yoyo.setDeltaMovement(context.velocity.x, context.velocity.y, context.velocity.z);
    rotateYoyo(yoyo, context);
  }

  private void collideWithShapes(YoyoEntity yoyo, YoyoContext context, List<YoyoBehavior> behaviors, List<VoxelShape> collisionShapes, AABB collisionHitBox) {
    if (collisionShapes.isEmpty()) {
      return;
    }
    AABB box = collisionHitBox.move(scratchB.x, scratchB.y, scratchB.z);
    double dx = scratchA.x;
    double dy = scratchA.y;
    double dz = scratchA.z;
    if (dy != 0.0) {
      dy = collideAndHandleTouch(Direction.Axis.Y, yoyo, context, behaviors, collisionShapes, box, dy);
      if (dy != 0.0) box = box.move(0.0, dy, 0.0);
    }
    boolean moreZ = Math.abs(dx) < Math.abs(dz);
    if (moreZ && dz != 0.0) {
      dz = collideAndHandleTouch(Direction.Axis.Z, yoyo, context, behaviors, collisionShapes, box, dz);
      if (dz != 0.0) box = box.move(0.0, 0.0, dz);
    }
    if (dx != 0.0) {
      dx = collideAndHandleTouch(Direction.Axis.X, yoyo, context, behaviors, collisionShapes, box, dx);
      if (!moreZ && dx != 0.0) box = box.move(dx, 0.0, 0.0);
    }
    if (!moreZ && dz != 0.0) dz = collideAndHandleTouch(Direction.Axis.Z, yoyo, context, behaviors, collisionShapes, box, dz);
    scratchA.set(dx, dy, dz);
  }

  private double collideAndHandleTouch(Direction.Axis axis, YoyoEntity yoyo, YoyoContext context, List<YoyoBehavior> behaviors, List<VoxelShape> collisionShapes, AABB box, double delta) {
    double resolved = Shapes.collide(axis, box, collisionShapes, delta);
    if (resolved != delta) {
      scratchC.set(context.ourPos).add(scratchB);
      int sign = Mth.sign(delta);
      if (sign > 0) set(axis, scratchC, box.max(axis) + resolved + Mth.EPSILON);
      if (sign < 0) set(axis, scratchC, box.min(axis) + resolved - Mth.EPSILON);
      BlockPos hitPos = BlockPos.containing(scratchC.x, scratchC.y, scratchC.z);
      for (YoyoBehavior behavior : behaviors) behavior.onTouchBlock(yoyo, context, hitPos, scratchC);
    }
    return resolved;
  }

  private static void set(Direction.Axis axis, Vector3d vector, double value) {
    switch (axis) {
      case X -> vector.x = value;
      case Y -> vector.y = value;
      case Z -> vector.z = value;
    }
  }

  private static void hitAndFilterEntities(YoyoEntity yoyo, YoyoContext context, List<YoyoBehavior> behaviors, List<Entity> entities, AABB box) {
    entities.removeIf(entity -> {
      if (!entity.getBoundingBox().intersects(box)) return false;
      for (YoyoBehavior behavior : behaviors) behavior.onCollide(yoyo, entity, context);
      return true;
    });
  }

  private void rotateYoyo(YoyoEntity yoyo, YoyoContext context) {
    Vector3d point = scratchA.set(context.ourPos).sub(context.tailPos);
    if (point.lengthSquared() == 0.0) return;
    double xzShadow = Math.sqrt(Math.fma(point.x, point.x, point.z * point.z));
    yoyo.setYRot((float) (Mth.atan2(point.z, point.x) * 180.0 / Math.PI) + 90.0F);
    yoyo.setXRot((float) (Mth.atan2(xzShadow, point.y) * 180.0 / Math.PI) - 90.0F);
    while (yoyo.getXRot() - yoyo.xRotO < -180.0F) yoyo.xRotO -= 360.0F;
    while (yoyo.getXRot() - yoyo.xRotO >= 180.0F) yoyo.xRotO += 360.0F;
    while (yoyo.getYRot() - yoyo.yRotO < -180.0F) yoyo.yRotO -= 360.0F;
    while (yoyo.getYRot() - yoyo.yRotO >= 180.0F) yoyo.yRotO += 360.0F;
    yoyo.setXRot(Mth.lerp(0.5F, yoyo.xRotO, yoyo.getXRot()));
    yoyo.setYRot(Mth.lerp(0.5F, yoyo.yRotO, yoyo.getYRot()));
  }
}
