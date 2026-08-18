package modernmods.modernfoundry.tools.modules.ranged.common;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import modernmods.mantle.data.loadable.primitive.BooleanLoadable;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.entity.ReusableProjectile;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.shared.TinkerCommons;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that places a glow after hitting the target
 * @param damage     Damage to deal when placing a glow
 * @param blocks     If true, places glow on hitting a block
 * @param entities   If true, places glow at the feet of the hit entity
 */
public record ProjectilePlaceGlowModule(int damage, boolean blocks, boolean entities) implements ModifierModule, ProjectileHitModifierHook, ProjectileLaunchModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ProjectilePlaceGlowModule>defaultHooks(ModifierHooks.PROJECTILE_HIT, ModifierHooks.PROJECTILE_LAUNCH);
  public static final RecordLoadable<ProjectilePlaceGlowModule> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ZERO.requiredField("tool_damage", ProjectilePlaceGlowModule::damage),
    BooleanLoadable.INSTANCE.requiredField("blocks", ProjectilePlaceGlowModule::blocks),
    BooleanLoadable.INSTANCE.requiredField("entities", ProjectilePlaceGlowModule::entities),
    ProjectilePlaceGlowModule::new);

  @Override
  public RecordLoadable<ProjectilePlaceGlowModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    // deal damage to the bow if it added glowing to its arrow
    // don't damage fishing hooks though, we will do that on hit
    if (primary && damage > 0) {
      ToolDamageUtil.damageLauncher(tool, damage, shooter, projectile, modifier.getId());
    }
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    if (entities) {
      TinkerCommons.glowBlock.get().addGlow(projectile.level(), hit.getEntity().blockPosition(), Direction.DOWN);
      if (damage > 0) {
        ModifierUtil.updateFishingRod(projectile, damage, false, modifier.getId());
      }
    }
    return false;
  }

  @Override
  public boolean onProjectileHitsBlock(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, BlockHitResult hit, @Nullable LivingEntity owner) {
    if (blocks) {
      Direction direction = hit.getDirection();
      if (TinkerCommons.glowBlock.get().addGlow(projectile.level(), hit.getBlockPos().relative(direction), direction.getOpposite())) {
        ModifierUtil.updateFishingRod(projectile, damage, true, modifier.getId());
        ReusableProjectile.discard(projectile);
      }
    }
    return false;
  }
}
