package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3dc;

/** One independently composable yoyo behavior. */
public interface YoyoBehavior {
  default void tick(YoyoEntity yoyo) {}

  default void onTouchBlock(YoyoEntity yoyo, YoyoContext context, BlockPos touchedPos, Vector3dc touchedExact) {}

  default void onCollide(YoyoEntity yoyo, Entity other, YoyoContext context) {}
}
