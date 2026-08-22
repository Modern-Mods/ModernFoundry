package modernmods.modernfoundry.integrations.items.modifiers.tool;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class FlamedModifier extends NoLevelsModifier implements ProjectileLaunchModifierHook, ProjectileHitModifierHook, MeleeHitModifierHook {

    @Override
    protected void registerHooks(Builder builder) {
        builder.addHook(this, ModifierHooks.PROJECTILE_HIT, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.MELEE_HIT);
    }

    @Override
    public float beforeMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damage, float baseKnockback, float knockback) {
        LivingEntity target = context.getLivingTarget();

        if (target != null && !target.isOnFire()) {
            target.setRemainingFireTicks(1);
        }

        return knockback + 1F;
    }

    @Override
    public void failedMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageAttempted) {
        LivingEntity target = context.getLivingTarget();

        if (target != null && target.isOnFire()) {
            target.clearFire();
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        doSecondaryDamage(context.getTarget(), context.getLivingTarget());
    }

    @Override
    public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
        // Apply knockback
        if (hit.getEntity() instanceof LivingEntity living) {
            if (projectile instanceof AbstractArrow arrow) {
                Vec3 knockbackVec = arrow.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(1F * 0.6D);

                if (knockbackVec.lengthSqr() > 0.0D) {
                    living.push(knockbackVec.x(), 0.1D, knockbackVec.z());
                }
            }
            doSecondaryDamage(hit.getEntity(), living);
        }

        return false;
    }

    private void doSecondaryDamage(Entity entity, LivingEntity target) {
        if (OptionalIntegrationHelper.isEntity(target, "iceandfire:ice_dragon")) {
            ToolAttackUtil.attackEntitySecondary(entity.level().damageSources().inFire(), 13.5F, entity, target, false);
        }
        if (target != null) {
            target.setRemainingFireTicks(5 * 20);
        }
    }

    @Override
    public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
        projectile.setRemainingFireTicks(5 * 20);
    }

}
