package modernmods.modernfoundry.world.item;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

/** Head item for enderman head, which counts as a pumpkin on the head */
public class EndermanHeadItem extends StandingAndWallBlockItem {
  public EndermanHeadItem(Block pBlock, Block pWallBlock, Properties pProperties, Direction pAttachmentDirection) {
    super(pBlock, pWallBlock, pAttachmentDirection, pProperties);
  }

  @Override
  public boolean isGazeDisguise(ItemStack stack, Player player, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity entity) {
    // 26.1.2 replaced isEnderMask with the generic gaze-disguise hook; the enderman head still hides the wearer's gaze
    return true;
  }
}
