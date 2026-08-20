package modernmods.modernfoundry.tools.yoyo.control;

import modernmods.modernfoundry.tools.yoyo.YoyoBehavior;
import modernmods.modernfoundry.tools.yoyo.YoyoContext;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.world.entity.Entity;

public final class RetractBehavior implements YoyoBehavior {
  @Override
  public void onCollide(YoyoEntity yoyo, Entity other, YoyoContext context) {
    if (yoyo.isEntityOwnerOrOwnersMount(other)) yoyo.discard();
  }
}
