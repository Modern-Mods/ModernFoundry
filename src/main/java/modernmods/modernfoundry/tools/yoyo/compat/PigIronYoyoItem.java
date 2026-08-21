package modernmods.modernfoundry.tools.yoyo.compat;

import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import modernmods.modernfoundry.tools.yoyo.YoyoTier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PigIronYoyoItem extends YoyoItem {
  public PigIronYoyoItem(YoyoTier tier) { super(tier); }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide) {
      YoyoEntity yoyo = YoyoEntity.CASTERS.get(player.getUUID());
      if (yoyo == null) {
        yoyo = this.factory.create(level, player, hand);
        level.addFreshEntity(yoyo);
        level.playSound(null, yoyo.getX(), yoyo.getY(), yoyo.getZ(), SoundEvents.PIG_HURT, SoundSource.NEUTRAL, 0.5F, 1.0F);
        player.causeFoodExhaustion(0.05F);
      } else {
        yoyo.setRetracting(!yoyo.isRetracting());
      }
    }
    return InteractionResultHolder.success(stack);
  }

  @Override
  public int getCordColor(ItemStack yoyo, float ticks) { return 10964513; }
}
