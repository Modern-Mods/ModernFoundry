package modernmods.modernfoundry.tools.yoyo.control;

import modernmods.modernfoundry.tools.yoyo.YoyoContext;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import modernmods.modernfoundry.tools.yoyo.YoyoTarget;
import net.minecraft.world.entity.Entity;

public enum RetractTarget implements YoyoTarget {
  INSTANCE;

  @Override
  public void updateTarget(YoyoEntity yoyo, Entity owner, YoyoContext context) {
    context.targetPos.set(context.tailPos);
  }
}
