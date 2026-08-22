package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class OverFreezeModifier extends Modifier implements MeleeHitModifierHook, ModifyDamageModifierHook{
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT,ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.isExtraAttack() && context.isFullyCharged()) {
            LivingEntity target = context.getLivingTarget();
            int x = Math.min(5,OverslimeModule.INSTANCE.getAmount(tool));
            if (target != null && x>0) {
                OverslimeModule.INSTANCE.removeAmount(tool, modifier,x);
                target.addEffect(new MobEffectInstance(ModEffects.holder(ModEffects.freezing_cold),40*x,modifier.getLevel()));
                target.setLastHurtMob(context.getAttacker());
            }
        }
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, net.minecraft.world.entity.EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        Entity attacker = source.getEntity();
        int x = Math.min(5,OverslimeModule.INSTANCE.getAmount(tool));
        if (attacker instanceof LivingEntity living&& attacker.isAlive() && x>0) {
            OverslimeModule.INSTANCE.removeAmount(tool, modifier,x);
            living.addEffect(new MobEffectInstance(ModEffects.holder(ModEffects.freezing_cold), 40*x, modifier.getLevel()));
            living.setLastHurtMob(context.getEntity());
        }
        return amount;
    }
}
