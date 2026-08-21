package modernmods.modernfoundry.tools.yoyo.api;

import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockInteraction {
  boolean apply(ItemStack stack, Player player, BlockPos pos, BlockState state, Block block, YoyoEntity yoyo);
}
