package modernmods.modernfoundry.shared.item;

import net.minecraft.ChatFormatting;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.TConstruct;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class CheeseItem extends Item {
  public static final Component TOOLTIP = TConstruct.makeTranslation("item", "cheese.tooltip").withStyle(ChatFormatting.GRAY);
  public CheeseItem(Properties pProperties) {
    super(pProperties);
  }

  /** Removes a random effect from the given entity */
  public static void removeRandomEffect(LivingEntity living) {
    if (!living.level().isClientSide()) {
      Collection<MobEffectInstance> effects = living.getActiveEffects();
      if (!effects.isEmpty()) {
        // milk cure equates to clearing any active effect since the cure registry was removed in 26.1
        List<Holder<net.minecraft.world.effect.MobEffect>> removable = effects.stream().map(MobEffectInstance::getEffect).toList();
        if (!removable.isEmpty()) {
          living.removeEffect(removable.get(living.getRandom().nextInt(removable.size())));
        }
      }
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    removeRandomEffect(living);
    return super.finishUsingItem(stack, level, living);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag pIsAdvanced) {
    List<Component> tooltip = new java.util.ArrayList<>();
    tooltip.add(TOOLTIP);
  
    tooltip.forEach(tooltipConsumer);
  }
}
