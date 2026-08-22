package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class ShieldOfTheDeepModifier extends NoLevelsModifier implements OnAttackedModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (context.getEntity() instanceof Player player
                && !player.level().isClientSide
                && source.getEntity() instanceof LivingEntity attacker
                && isDirectDamage) {
            final ServerPlayer sp = (ServerPlayer) player;

            Holder<MobEffect> exsanguination = OptionalIntegrationHelper.effectHolder(
                    "com.github.alexthe666.alexsmobs.effect.AMEffectRegistry", "EXSANGUINATION", "alexsmobs:exsanguination");
            if (exsanguination != null && sp.distanceTo(attacker) <= 4
                    && !attacker.hasEffect(exsanguination)) {
                attacker.addEffect(new MobEffectInstance(exsanguination, 60, 2));
            }
            if (sp.isInWaterOrBubble()) {
                sp.setAirSupply(Math.min(sp.getMaxAirSupply(), sp.getMaxAirSupply() + 150));
            }
        }
    }

}

