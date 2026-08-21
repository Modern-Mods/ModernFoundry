package modernmods.modernfoundry.tools.yoyo.api;

import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface YoyoFactory {
  YoyoEntity create(Level level, Player player, InteractionHand hand);
}
