package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/** @deprecated use {@link modernmods.modernfoundry.library.modifiers.modules.combat.MobEffectModule.ArmorCounter} and {@link modernmods.modernfoundry.tools.modules.ClearEffectOnUnequipModule} */
@Deprecated(forRemoval = true)
public class RevengeModifier extends NoLevelsModifier implements EquipmentChangeModifierHook, OnAttackedModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.ON_ATTACKED);
  }

  @Override
  public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    // must be attacked by entity
    Entity trueSource = source.getEntity();
    LivingEntity living = context.getEntity();
    if (trueSource != null && trueSource != living) { // no making yourself mad with slurping or self-destruct or alike
      MobEffectInstance effect = new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300);
      living.addEffect(effect);
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (context.getChangedSlot() == EquipmentSlot.HEAD) {
      IToolStackView replacement = context.getReplacementTool();
      if (replacement == null || replacement.getModifierLevel(this) == 0) {
        context.getEntity().removeEffect(MobEffects.DAMAGE_BOOST);
      }
    }
  }
}
