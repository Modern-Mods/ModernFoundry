package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.tools.TinkerTools;

public final class YoyoItem extends TieredItem {
  public YoyoItem(Tier tier, Properties properties) {
    super(tier, properties);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide) {
      YoyoTracker tracker = YoyoTracker.on(player);
      YoyoEntity yoyo = tracker.getYoyoInHand(hand);
      if (yoyo != null) {
        yoyo.sendRetract();
      } else {
        YoyoEntity thrown = new YoyoEntity(level);
        thrown.onThrow(player, hand);
        level.addFreshEntity(thrown);
        tracker.setYoyoInHand(hand, thrown);
      }
    }
    player.awardStat(Stats.ITEM_USED.get(this));
    return InteractionResultHolder.success(player.getItemInHand(hand));
  }
}
