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

/** @deprecated use {@link modernmods.modernfoundry.library.modifiers.modules.combat.MobEffectModule.ArmorAttack} */
@Deprecated(forRemoval = true)
public class FrosttouchModifier extends NoLevelsModifier implements DamageDealtModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.DAMAGE_DEALT);
    hookBuilder.addModule(StrongBonesModifier.CALCIFIABLE_MODULE);
  }

  @Override
  public void onDamageDealt(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, LivingEntity target, DamageSource source, float amount, boolean isDirectDamage) {
    // must drink milk to melee slowness. Always can range slowness
    if (isDirectDamage) {
      boolean isCalcified = context.getEntity().hasEffect(TinkerEffects.holder(TinkerModifiers.calcifiedEffect));
      if (isCalcified || source.is(DamageTypeTags.IS_PROJECTILE)) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, isCalcified ? 1 : 0), context.getEntity());
      }
    }
  }
}
