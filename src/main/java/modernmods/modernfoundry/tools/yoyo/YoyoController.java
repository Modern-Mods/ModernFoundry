package modernmods.modernfoundry.tools.yoyo;

import modernmods.modernfoundry.tools.yoyo.control.AttackBehavior;
import modernmods.modernfoundry.tools.yoyo.control.LookTarget;
import modernmods.modernfoundry.tools.yoyo.control.MotionResolver;
import modernmods.modernfoundry.tools.yoyo.control.Mover;
import modernmods.modernfoundry.tools.yoyo.control.ParticleSprayBehavior;
import modernmods.modernfoundry.tools.yoyo.control.RetractBehavior;
import modernmods.modernfoundry.tools.yoyo.control.RetractTarget;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public final class YoyoController {
  private final YoyoContext context = new YoyoContext();
  private final List<YoyoBehavior> behaviors = new ArrayList<>();
  private final Mover mover = new Mover();
  private final MotionResolver motionResolver = new MotionResolver();
  private YoyoTarget target = new LookTarget();
  private boolean retracting;

  public void tick(YoyoEntity yoyo) {
    Entity owner = yoyo.getOwner();
    if (owner == null) return;
    for (YoyoBehavior behavior : behaviors) behavior.tick(yoyo);
    yoyo.getCenterPos(context.ourPos);
    yoyo.getOwnerEyePos(context.tailPos);
    target.updateTarget(yoyo, owner, context);
    mover.tick(yoyo, context);
    motionResolver.solve(yoyo, context, behaviors);
  }

  public void onThrow(YoyoEntity yoyo, Entity owner) {
    yoyo.setPos(owner.getX(), owner.getY() + owner.getEyeHeight() * 0.75, owner.getZ());
    behaviors.add(new AttackBehavior());
    behaviors.add(new ParticleSprayBehavior());
  }

  public void signalRetract() {
    if (retracting) return;
    retracting = true;
    target = RetractTarget.INSTANCE;
    behaviors.add(new RetractBehavior());
  }
}
