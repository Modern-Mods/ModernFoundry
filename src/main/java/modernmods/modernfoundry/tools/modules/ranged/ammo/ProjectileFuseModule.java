package modernmods.modernfoundry.tools.modules.ranged.ammo;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.entity.ReusableProjectile;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ScheduledProjectileTaskModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.EntityModifierCapability;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.utils.Schedule.Scheduler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that makes the projectile expire after a short time.
 * @param time  Time the projectile expires.
 */
public record ProjectileFuseModule(SimpleParticleType particle, LevelingInt time) implements ModifierModule, ScheduledProjectileTaskModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ProjectileGravityModule>defaultHooks(ModifierHooks.SCHEDULE_PROJECTILE_TASK);
  public static final RecordLoadable<ProjectileFuseModule> LOADER = RecordLoadable.create(
    TinkerLoadables.SIMPLE_PARTICLE.requiredField("particle", ProjectileFuseModule::particle),
    LevelingInt.LOADABLE.requiredField("time", ProjectileFuseModule::time),
    ProjectileFuseModule::new);


  @Override
  public RecordLoadable<ProjectileFuseModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void scheduleProjectileTask(IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, Scheduler scheduler) {
    scheduler.add(0, time.computeForLevel(modifier.getEffectiveLevel()));
  }

  @Override
  public void onScheduledProjectileTask(IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, int task) {
    if (task == 0 && !projectile.isRemoved()) {
      // alert other modifiers that the projectile is gone, lets some of them perform an early action
      // include bow modifiers here, as it adds a bit more flexibility
      ModifierNBT modifiers = EntityModifierCapability.getOrEmpty(projectile);
      for (ModifierEntry entry : modifiers) {
        entry.getHook(ModifierHooks.PROJECTILE_FUSE).onProjectileFuseFinish(modifiers, persistentData, entry, ammo, projectile, arrow);
      }

      if (!projectile.level().isClientSide()) {
        // fuse animation
        Vec3 position = projectile.position();
        if (projectile.level() instanceof ServerLevel level) {
          level.sendParticles(particle, position.x, position.y, position.z, 10, 0.0D, 0.0D, 0.0D, 0.1f);
        }

        // if it's reusable, don't discard, but rather zero momentum
        if (ReusableProjectile.isSingleUse(projectile)) {
          projectile.discard();
        } else {
          projectile.setDeltaMovement(Vec3.ZERO);
        }
      }
    }
  }
}
