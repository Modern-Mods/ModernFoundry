package modernmods.modernfoundry.tools.yoyo.compat;

import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import modernmods.modernfoundry.tools.yoyo.YoyoTier;
import net.minecraft.world.item.ItemStack;

public final class AdAstraYoyoItem extends YoyoItem {
  public AdAstraYoyoItem(YoyoTier tier) { super(tier); }

  @Override
  public int getCordColor(ItemStack yoyo, float ticks) { return 15773733; }
}
