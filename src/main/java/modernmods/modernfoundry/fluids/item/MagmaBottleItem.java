package modernmods.modernfoundry.fluids.item;

import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/** Magma bottle instance, which lights the drinker on fire */
public class MagmaBottleItem extends Item {
  private final int fireTime;
  public MagmaBottleItem(Properties props, int fireTime) {
    super(props);
    this.fireTime = fireTime;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flagIn) {
    List<Component> tooltip = new java.util.ArrayList<>();
    super.appendHoverText(stack, context, tooltipDisplay, tooltip::add, flagIn);
    tooltip.add(Component.translatable(
      "potion.withDuration",
      Blocks.FIRE.getName(),
      StringUtil.formatTickDuration(fireTime * 20, 20.0f)
    ).withStyle(MobEffectCategory.HARMFUL.getTooltipFormatting()));
  
    tooltip.forEach(tooltipConsumer);
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    player.startUsingItem(hand);
    return InteractionResult.CONSUME;
  }

  @Override
  public int getUseDuration(ItemStack pStack, LivingEntity entity) {
    return 32;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack pStack) {
    return ItemUseAnimation.DRINK;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    living.igniteForSeconds(fireTime);
    ItemStackTemplate remainderTemplate = stack.getItem().getCraftingRemainder(stack);
    ItemStack container = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
    Player player = living instanceof Player p ? p : null;
    if (player == null || !player.getAbilities().instabuild) {
      stack.shrink(1);
      container = container.copy();
      if (stack.isEmpty()) {
        return container;
      }
      if (player != null) {
        if (!player.getInventory().add(container)) {
          player.drop(container, false);
        }
      }
    }
    return stack;
  }
}
