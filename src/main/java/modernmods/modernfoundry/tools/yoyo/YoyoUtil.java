package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nullable;

/** Small geometry helpers kept local to the yoyo simulation. */
public final class YoyoUtil {
  private static final double EPSILON = 1.0E-7;

  private YoyoUtil() {}

  public static Vector3d storeEntityCenter(Vector3d dest, Entity entity) {
    return dest.set(entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ());
  }

  public static Vector3d storeEntityViewVec(Vector3d dest, Entity entity, float partialTick) {
    float xRot = entity.getViewXRot(partialTick) * (float) (Math.PI / 180.0);
    float yRot = -entity.getViewYRot(partialTick) * (float) (Math.PI / 180.0);
    float cosY = Mth.cos(yRot);
    float sinY = Mth.sin(yRot);
    float cosX = Mth.cos(xRot);
    float sinX = Mth.sin(xRot);
    return dest.set(sinY * cosX, -sinX, cosY * cosX);
  }

  public static Vector3d set(Vector3d dest, Vec3 source) {
    return dest.set(source.x, source.y, source.z);
  }

  public static Vec3 asVec3(Vector3d vector) {
    return new Vec3(vector.x, vector.y, vector.z);
  }

  public static boolean clip(AABB box, Vector3dc start, Vector3dc end, Vector3d dest) {
    double[] percentage = {1.0};
    Direction direction = getDirection(box, start, percentage, end.x() - start.x(), end.y() - start.y(), end.z() - start.z());
    if (direction == null) {
      return false;
    }
    double pct = percentage[0];
    dest.set(start).add(pct * (end.x() - start.x()), pct * (end.y() - start.y()), pct * (end.z() - start.z()));
    return true;
  }

  @Nullable
  private static Direction getDirection(AABB box, Vector3dc start, double[] percentage, double dx, double dy, double dz) {
    Direction direction = null;
    if (dx > EPSILON) {
      direction = clipPoint(percentage, Direction.WEST, direction, dx, dy, dz, box.minX, box.minY, box.maxY, box.minZ, box.maxZ, start.x(), start.y(), start.z());
    } else if (dx < -EPSILON) {
      direction = clipPoint(percentage, Direction.EAST, direction, dx, dy, dz, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ, start.x(), start.y(), start.z());
    }
    if (dy > EPSILON) {
      direction = clipPoint(percentage, Direction.DOWN, direction, dy, dz, dx, box.minY, box.minZ, box.maxZ, box.minX, box.maxX, start.y(), start.z(), start.x());
    } else if (dy < -EPSILON) {
      direction = clipPoint(percentage, Direction.UP, direction, dy, dz, dx, box.maxY, box.minZ, box.maxZ, box.minX, box.maxX, start.y(), start.z(), start.x());
    }
    if (dz > EPSILON) {
      direction = clipPoint(percentage, Direction.NORTH, direction, dz, dx, dy, box.minZ, box.minX, box.maxX, box.minY, box.maxY, start.z(), start.x(), start.y());
    } else if (dz < -EPSILON) {
      direction = clipPoint(percentage, Direction.SOUTH, direction, dz, dx, dy, box.maxZ, box.minX, box.maxX, box.minY, box.maxY, start.z(), start.x(), start.y());
    }
    return direction;
  }

  @Nullable
  private static Direction clipPoint(
    double[] percentage, Direction clipAxis, @Nullable Direction otherwise,
    double da, double db, double dc, double boundA,
    double minB, double maxB, double minC, double maxC,
    double startA, double startB, double startC
  ) {
    double aPct = (boundA - startA) / da;
    double bPos = startB + aPct * db;
    double cPos = startC + aPct * dc;
    if (aPct > 0.0 && aPct < percentage[0] && epsilonWithin(bPos, minB, maxB) && epsilonWithin(cPos, minC, maxC)) {
      percentage[0] = aPct;
      return clipAxis;
    }
    return otherwise;
  }

  private static boolean epsilonWithin(double value, double lower, double upper) {
    return lower - EPSILON < value && value < upper + EPSILON;
  }
}
