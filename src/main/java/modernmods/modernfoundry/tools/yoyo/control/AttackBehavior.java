package modernmods.modernfoundry.tools.yoyo.control;

import modernmods.modernfoundry.tools.yoyo.YoyoBehavior;
import modernmods.modernfoundry.tools.yoyo.YoyoContext;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import modernmods.modernfoundry.tools.yoyo.YoyoUtil;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;

public final class AttackBehavior implements YoyoBehavior {
  private static final float DAMAGE = 4.0F;
  private static final int COOLDOWN_TICKS = 15;
  private static final int COOLDOWN_GRACE_TICKS = 3;
  private int remainingCooldown;
  private int remainingGrace;
  private final Vector3d scratch = new Vector3d();

  @Override
  public void tick(YoyoEntity yoyo) {
    if (remainingCooldown > 0) {
      remainingCooldown--;
      return;
    }
    if (remainingGrace > 0 && --remainingGrace <= 0) remainingCooldown = COOLDOWN_TICKS;
  }

  @Override
  public void onCollide(YoyoEntity yoyo, Entity other, YoyoContext context) {
    if (remainingCooldown > 0 || yoyo.isEntityOwnerOrOwnersMount(other)) return;
    if (!other.level().isClientSide) other.hurt(yoyo.damageSources().cactus(), DAMAGE);
    YoyoUtil.storeEntityCenter(scratch, other).sub(context.ourPos).normalize(-0.5 * YoyoContext.INV_MASS).add(context.velocity, context.velocity);
    if (remainingGrace <= 0) remainingGrace = COOLDOWN_GRACE_TICKS;
  }
}
