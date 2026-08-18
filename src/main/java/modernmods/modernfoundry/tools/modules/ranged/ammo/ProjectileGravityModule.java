package modernmods.modernfoundry.tools.modules.ranged.ammo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ScheduledProjectileTaskModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.utils.Schedule.Scheduler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module making a projectile have no gravity right after throwing.
 * @param delay  Delay until the projectile receives gravity.
 */
public record ProjectileGravityModule(LevelingInt delay) implements ModifierModule, ProjectileShootModifierHook, ScheduledProjectileTaskModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ProjectileGravityModule>defaultHooks(ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_THROWN, ModifierHooks.SCHEDULE_PROJECTILE_TASK);
  public static final RecordLoadable<ProjectileGravityModule> LOADER = RecordLoadable.create(LevelingInt.LOADABLE.directField(ProjectileGravityModule::delay), ProjectileGravityModule::new);

  @Override
  public RecordLoadable<ProjectileGravityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    projectile.setNoGravity(true);
  }

  @Override
  public void scheduleProjectileTask(IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, Scheduler scheduler) {
    scheduler.add(0, delay.computeForLevel(modifier.getEffectiveLevel()));
  }

  @Override
  public void onScheduledProjectileTask(IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, int task) {
    if (task == 0 && !projectile.level().isClientSide()) {
      projectile.setNoGravity(false);
    }
  }
}
