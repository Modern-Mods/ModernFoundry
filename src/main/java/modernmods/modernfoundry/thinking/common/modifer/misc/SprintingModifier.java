package modernmods.modernfoundry.thinking.common.modifer.misc;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingAngleModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingForceModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingLaunchModifierHook;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.SlimeBounceHandler;
import modernmods.modernfoundry.tools.TinkerToolActions;
import modernmods.modernfoundry.tools.modifiers.ability.sling.SlingModifier;

public class SprintingModifier extends SlingModifier implements ModifierUtils {
    @Override
    public @NotNull InteractionResult onToolUse(IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull Player player, @NotNull InteractionHand hand, @NotNull InteractionSource source) {
        if (!tool.isBroken() && source == InteractionSource.RIGHT_CLICK) {
            GeneralInteractionModifierHook.startUsingWithDrawtime(tool, modifier.getId(), player, hand, 1f);
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public void beforeReleaseUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
        Level level = entity.level();
        if (entity instanceof Player player && !player.isFallFlying()) {
            player.causeFoodExhaustion(0.2F);
            float charge = getCharge(tool, modifier, timeLeft);
            if (charge > 0) {
                float multiplier = charge * 3.5F;
                float force = SlingForceModifierHook.modifySlingForce(tool, entity, entity, modifier, this.getPower(tool, player) * multiplier, multiplier);
                if (force > 0) {
                    Vec3 look = player.getLookAngle().add(0, 1, 0).normalize();
                    Vec3 angle = SlingAngleModifierHook.modifySlingAngle(tool, entity, entity, modifier, force, multiplier, new Vec3((look.x ), 0, (look.z )));
                    Vec3 velocity = player.getDeltaMovement();
                    player.setDeltaMovement(velocity.x, 0, velocity.z);
                    player.push(force * angle.x, 0.02f, force * angle.z);
                    int time = (int) (charge * 20);
                    addEffect(player, ModEffects.jumpless.get(), time, 0, false);
                    addEffect(player, ModEffects.weightless.get(),time, 1, false);
                    SlingLaunchModifierHook.afterSlingLaunch(tool, entity, entity, modifier, force, multiplier, angle);
                    SlimeBounceHandler.addBounceHandler(player);
                    if (!level.isClientSide) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), Sounds.SLIME_SLING.getSound(), player.getSoundSource(), 1, 1);
                        player.causeFoodExhaustion(0.2F);
                        player.getCooldowns().addCooldown(tool.getItem(), 3);
                        ToolDamageUtil.damageAnimated(tool, 1, entity);
                    }
                    if (ModifierUtil.canPerformAction(tool, TinkerToolActions.DRILL_ATTACK)) {
                        player.startAutoSpinAttack(20, 8.0F, player.getMainHandItem());
                    }
                    return;
                }
            }
        }
        if (isActive(tool, modifier, activeModifier)) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.SLIME_SLING.getSound(), entity.getSoundSource(), 1, 0.5f);
        }
    }
}
