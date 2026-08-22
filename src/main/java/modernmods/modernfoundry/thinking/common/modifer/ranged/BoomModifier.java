package modernmods.modernfoundry.thinking.common.modifer.ranged;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ScheduledProjectileTaskModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.utils.Schedule;

import javax.annotation.Nullable;

public class BoomModifier extends Modifier implements ScheduledProjectileTaskModifierHook,ProjectileHitModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.SCHEDULE_PROJECTILE_TASK,ModifierHooks.PROJECTILE_HIT);
    }
    @Override
    public void scheduleProjectileTask(IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @org.jetbrains.annotations.Nullable AbstractArrow arrow, ModDataNBT persistentData, Schedule.Scheduler scheduler) {
        scheduler.add(0,200);
    }

    @Override
    public void onScheduledProjectileTask(IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @org.jetbrains.annotations.Nullable AbstractArrow arrow, ModDataNBT persistentData, int task) {
        if (task == 0){
            boom(projectile,modifier.getLevel());
        }
    }
    @Override
    public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target, boolean notBlocked) {
        if (target != null&& attacker != null) {
            target.setLastHurtMob(attacker);
        }
        boom(projectile,modifier.getLevel());
        return false;
    }
    private void boom(Projectile projectile,int level){
        projectile.level().explode(projectile, projectile.getX(), projectile.getY(), projectile.getZ(), level, Level.ExplosionInteraction.MOB);
        projectile.discard();
    }
}
