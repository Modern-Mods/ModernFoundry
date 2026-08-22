package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import modernmods.modernfoundry.integrations.items.modifiers.ArsNouveauBaseModifier;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class EnchantersShieldModifier extends ArsNouveauBaseModifier implements OnAttackedModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (context.getEntity() instanceof Player player
                && !player.level().isClientSide
                && player.isBlocking()
                && source.getEntity() instanceof LivingEntity
                && isDirectDamage) {
            final ServerPlayer sp = (ServerPlayer) player;

            Holder<MobEffect> manaRegen = OptionalIntegrationHelper.effectHolder(
                    "com.hollingsworth.arsnouveau.setup.registry.ModPotions", "MANA_REGEN_EFFECT", "ars_nouveau:mana_regen");
            Holder<MobEffect> spellDamage = OptionalIntegrationHelper.effectHolder(
                    "com.hollingsworth.arsnouveau.setup.registry.ModPotions", "SPELL_DAMAGE_EFFECT", "ars_nouveau:spell_damage");
            if (manaRegen != null) sp.addEffect(new MobEffectInstance(manaRegen, 200, 1));
            if (spellDamage != null) sp.addEffect(new MobEffectInstance(spellDamage, 200, 1));
        }
    }

}

