package modernmods.modernfoundry.world.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Native Oreberries item with a tintable color and optional essence XP use. */
public class OreberryItem extends Item {
  private final String name;
  private final int color;
  private final boolean essence;

  public OreberryItem(Properties properties, String name, int color, boolean essence) {
    super(properties);
    this.name = name;
    this.color = color;
    this.essence = essence;
  }

  public int color() {
    return color;
  }

  @Override
  public Component getName(ItemStack stack) {
    return Component.translatable("item.modernfoundry." + name);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("item.modernfoundry." + name + ".tooltip"));
    super.appendHoverText(stack, context, tooltip, flag);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!essence) {
      return super.use(level, player, hand);
    }

    if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
      ExperienceOrb.award(serverLevel, player.position(), level.getRandom().nextInt(14) + 6);
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }
    player.awardStat(Stats.ITEM_USED.get(this));
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
  }
}
