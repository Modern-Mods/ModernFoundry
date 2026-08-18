package modernmods.modernfoundry.fluids.item;

import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ContainerFoodItem extends Item {
  public ContainerFoodItem(Properties props) {
    super(props);
  }

  @Override
  public int getUseDuration(ItemStack pStack, LivingEntity entity) {
    return 32;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack pStack) {
    return ItemUseAnimation.DRINK;
  }

  /** Adds effects to the tooltip */
  public static void addEffectTooltip(Consumable consumable, List<Component> tooltip) {
    // add effects to the tooltip, code based on potion items
    for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
      if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect apply) {
        for (MobEffectInstance effect : apply.effects()) {
          MutableComponent mutable = Component.translatable(effect.getDescriptionId());
          if (effect.getAmplifier() > 0) {
            mutable = Component.translatable("potion.withAmplifier", mutable, Component.translatable("potion.potency." + effect.getAmplifier()));
          }
          if (effect.getDuration() > 20) {
            mutable = Component.translatable("potion.withDuration", mutable, MobEffectUtil.formatDuration(effect, 1.0f, 20.0f));
          }
          tooltip.add(mutable.withStyle(effect.getEffect().value().getCategory().getTooltipFormatting()));
        }
      }
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flagIn) {
    List<Component> tooltip = new java.util.ArrayList<>();
    Consumable consumable = stack.get(net.minecraft.core.component.DataComponents.CONSUMABLE);
    if (consumable != null) {
      addEffectTooltip(consumable, tooltip);
    }

    tooltip.forEach(tooltipConsumer);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    ItemStackTemplate remainderTemplate = stack.getItem().getCraftingRemainder(stack);
    ItemStack container = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
    ItemStack result = super.finishUsingItem(stack, level, living);
    Player player = living instanceof Player p ? p : null;
    if (player == null || !player.getAbilities().instabuild) {
      container = container.copy();
      if (result.isEmpty()) {
        return container;
      }
      if (player != null) {
        if (!player.getInventory().add(container)) {
          player.drop(container, false);
        }
      }
    }
    return result;
  }

  public static class FluidContainerFoodItem extends ContainerFoodItem {
    private final Supplier<FluidStack> fluid;
    public FluidContainerFoodItem(Properties props, Supplier<FluidStack> fluid) {
      super(props);
      this.fluid = fluid;
    }

    public FluidStack getFluid() {
      return fluid.get();
    }
  }
}
