package modernmods.modernfoundry.library.modifiers.fluid.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import modernmods.mantle.data.loadable.common.ItemStackLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.fluid.EffectLevel;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectContext;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectContext.Entity;

import java.util.Collection;
import java.util.List;

/**
 * Effect to clear all effects using the given stack. As of 26.1 the NeoForge cure registry was removed, so this applies
 * the configured item's own {@link Consumable} consume effects (milk clears all effects, honey removes poison-like effects).
 * @param stack  Stack used for curing, standard is milk bucket
 */
public record CureEffectsFluidEffect(ItemStack stack) implements FluidEffect<FluidEffectContext.Entity> {
  public static final RecordLoadable<CureEffectsFluidEffect> LOADER = RecordLoadable.create(ItemStackLoadable.REQUIRED_ITEM.requiredField("item", e -> e.stack), CureEffectsFluidEffect::new);

  public CureEffectsFluidEffect(ItemLike item) {
    this(new ItemStack(item));
  }

  /** Gets the consume effects for the configured curing item. */
  private List<ConsumeEffect> cureEffects() {
    Consumable consumable = stack.get(DataComponents.CONSUMABLE);
    return consumable != null ? consumable.onConsumeEffects() : List.of();
  }

  /** Checks whether any of the given consume effects would remove an active effect from the target. */
  private static boolean wouldCure(LivingEntity target, List<ConsumeEffect> effects) {
    Collection<MobEffectInstance> active = target.getActiveEffects();
    if (active.isEmpty()) {
      return false;
    }
    for (ConsumeEffect effect : effects) {
      if (effect instanceof ClearAllStatusEffectsConsumeEffect) {
        return true;
      }
      if (effect instanceof RemoveStatusEffectsConsumeEffect remove) {
        for (MobEffectInstance instance : active) {
          if (remove.effects().contains(instance.getEffect())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, Entity context, FluidAction action) {
    LivingEntity target = context.getLivingTarget();
    if (target != null && level.isFull()) {
      List<ConsumeEffect> effects = cureEffects();
      // when simulating, check whether the consume effects would remove any active effect
      if (action.simulate()) {
        return wouldCure(target, effects) ? 1 : 0;
      }
      Level entityLevel = target.level();
      boolean applied = false;
      for (ConsumeEffect effect : effects) {
        if (effect.apply(entityLevel, stack, target)) {
          applied = true;
        }
      }
      return applied ? 1 : 0;
    }
    return 0;
  }

  @Override
  public RecordLoadable<CureEffectsFluidEffect> getLoader() {
    return LOADER;
  }
}
