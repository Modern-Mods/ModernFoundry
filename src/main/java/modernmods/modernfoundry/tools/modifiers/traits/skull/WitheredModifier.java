package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.DamageDealtModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.TinkerModifiers;

@Deprecated
public class WitheredModifier extends NoLevelsModifier implements DamageDealtModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(StrongBonesModifier.CALCIFIABLE_MODULE);
    hookBuilder.addHook(this, ModifierHooks.DAMAGE_DEALT);
  }

  @Override
  public void onDamageDealt(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, LivingEntity target, DamageSource source, float amount, boolean isDirectDamage) {
    // drink milk for more power, but less duration
    if (isDirectDamage && !source.is(DamageTypeTags.IS_PROJECTILE)) {
      LivingEntity attacker = context.getEntity();
      boolean isCalcified = attacker.hasEffect(TinkerEffects.holder(TinkerModifiers.calcifiedEffect));
      target.addEffect(new MobEffectInstance(MobEffects.WITHER, isCalcified ? 100 : 200, isCalcified ? 1 : 0), attacker);
    }
  }
}
