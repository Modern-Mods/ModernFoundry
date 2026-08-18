package modernmods.modernfoundry.gadgets.item;

import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import modernmods.mantle.util.TranslationHelper;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.gadgets.entity.GlowballEntity;

import java.util.List;

/** @deprecated use {@link modernmods.modernfoundry.library.tools.item.ModifiableShurikenItem} with {@link modernmods.modernfoundry.tools.modules.ranged.common.ProjectilePlaceGlowModule} */
@Deprecated
public class GlowBallItem extends SnowballItem {

  public GlowBallItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult use(Level level, Player playerIn, InteractionHand handIn) {
    ItemStack itemstack = playerIn.getItemInHand(handIn);
    if (!playerIn.getAbilities().instabuild) {
      itemstack.shrink(1);
    }

    level.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), Sounds.THROWBALL_THROW.getSound(), SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
    if (!level.isClientSide()) {
      GlowballEntity glowballEntity = new GlowballEntity(level, playerIn);
      glowballEntity.setItem(itemstack);
      glowballEntity.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F, 1.0F);
      level.addFreshEntity(glowballEntity);
    }

    playerIn.awardStat(Stats.ITEM_USED.get(this));
    return InteractionResult.SUCCESS;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    List<Component> tooltip = new java.util.ArrayList<>();
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, context, tooltipDisplay, tooltip::add, flag);
  
    tooltip.forEach(tooltipConsumer);
  }
}
