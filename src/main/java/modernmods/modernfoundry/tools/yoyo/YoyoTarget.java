package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.world.entity.Entity;

public interface YoyoTarget {
  void updateTarget(YoyoEntity yoyo, Entity owner, YoyoContext context);
}
