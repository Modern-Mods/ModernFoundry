package modernmods.modernfoundry.tools.modules.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing melee and ranged effects of freezing */
public record FreezingAttackModule(LevelingValue time) implements ModifierModule, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, ProjectileHitModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FreezingAttackModule>defaultHooks(ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_HIT);
  public static final RecordLoadable<FreezingAttackModule> LOADER = RecordLoadable.create(
    LevelingValue.LOADABLE.requiredField("seconds", FreezingAttackModule::time),
    FreezingAttackModule::new);

  @Override
  public RecordLoadable<FreezingAttackModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /** Sets the target on fire */
  private void freeze(ModifierEntry modifier, Entity target) {
    if (target.canFreeze()) {
      target.setTicksFrozen(Math.max(target.getTicksRequiredToFreeze(), target.getTicksFrozen()) + (int)(time.compute(modifier.getEffectiveLevel()) * 40));
      target.setRemainingFireTicks(0);
    }
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    freeze(modifier, context.getTarget());
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    freeze(modifier, hit.getEntity());
    return false;
  }
}
