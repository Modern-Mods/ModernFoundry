package modernmods.modernfoundry.integrations.items.modifiers.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import modernmods.modernfoundry.integrations.items.modifiers.traits.ManaModifier;
import modernmods.modernfoundry.integrations.util.BotaniaHelper;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class TerraModifier extends ManaModifier implements MeleeHitModifierHook {

    private static final int MANA_PER_DAMAGE = 100;

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public int getManaPerDamage(ServerPlayer sp) {
        return BotaniaHelper.getManaPerDamageBonus(sp, MANA_PER_DAMAGE);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        final Player player = context.getPlayerAttacker() != null ? context.getPlayerAttacker() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            DamageSource source = sp.level().damageSources().indirectMagic(sp, null);
            ItemStack stack = sp.getItemInHand(InteractionHand.MAIN_HAND);

            if (sp.getAttackStrengthScale(0F) == 1 && BotaniaHelper.requestManaExactForTool(stack, sp, getManaPerDamage(sp) * 2, true)) {
                sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        OptionalIntegrationHelper.sound("botania:terra_blade"), SoundSource.PLAYERS, 1F, 1F);
                ToolAttackUtil.attackEntitySecondary(source, 7.0F, context.getTarget(), context.getLivingTarget(), true);
            }
        }
    }

}
