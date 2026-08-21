package modernmods.modernfoundry.tools.yoyo.api;

import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface EntityInteraction {
  boolean apply(ItemStack stack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target);
}
