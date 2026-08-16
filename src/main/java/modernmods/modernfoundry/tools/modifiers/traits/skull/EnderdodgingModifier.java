package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import modernmods.hilt.data.predicate.damage.DamageSourcePredicate;
import modernmods.modernfoundry.library.events.teleport.EnderdodgingTeleportEvent;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.TeleportHelper;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.modules.armor.TeleportDodgeModule;

/** @deprecated use {@link TeleportDodgeModule} */
@Deprecated
public class EnderdodgingModifier extends NoLevelsModifier implements OnAttackedModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(TeleportDodgeModule.builder().damageSource(DamageSourcePredicate.IS_INDIRECT).flat(15 * 20));
    hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
  }

  @Override
  public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    // teleport randomly from other damage
    LivingEntity self = context.getEntity();
    if (!self.hasEffect(TinkerEffects.holder(TinkerEffects.enderference)) && source.getEntity() instanceof LivingEntity && RANDOM.nextInt(10) == 0) {
      if (TeleportHelper.randomNearbyTeleport(context.getEntity(), (e, x, y, z) -> new EnderdodgingTeleportEvent(e, x, y, z, modifier))) {
        TinkerEffects.enderference.get().apply(self, 15 * 20, 1, true);
      }
    }
  }
}
