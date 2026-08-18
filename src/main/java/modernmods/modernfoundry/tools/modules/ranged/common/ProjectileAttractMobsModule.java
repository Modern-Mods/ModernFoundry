package modernmods.modernfoundry.tools.modules.ranged.common;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.entity.ReusableProjectile;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileFuseModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

import javax.annotation.Nullable;
import java.util.List;

/** Module that attracts mobs to the target of a projectile. */
public record ProjectileAttractMobsModule(LevelingValue radius, LevelingValue strength) implements ModifierModule, ProjectileHitModifierHook, ProjectileFuseModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ProjectileAttractMobsModule>defaultHooks(ModifierHooks.PROJECTILE_HIT, ModifierHooks.PROJECTILE_FUSE);
  public static final RecordLoadable<ProjectileAttractMobsModule> LOADER = RecordLoadable.create(
    LevelingValue.LOADABLE.requiredField("radius", ProjectileAttractMobsModule::radius),
    LevelingValue.LOADABLE.requiredField("strength", ProjectileAttractMobsModule::strength),
    ProjectileAttractMobsModule::new);

  @Override
  public RecordLoadable<ProjectileAttractMobsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /** Pulls all target mobs */
  private void pullMobs(ModifierEntry entry, Projectile projectile, Vec3 origin) {
    double x = origin.x;
    double y = origin.y;
    double z = origin.z;
    float level = entry.getEffectiveLevel();
    float range = radius.compute(level);
    List<LivingEntity> targets = projectile.level().getEntitiesOfClass(LivingEntity.class, new AABB(x - range, y - range, z - range, x + range, y + range, z + range));

    // only pull up to a max targets
    int pulled = 0;
    float strength = this.strength.compute(level);
    for (LivingEntity target : targets) {
      if (target.isRemoved() || target.position().distanceToSqr(origin) < 0.25f) {
        continue;
      }

      // pull the owner, bonus pulling if we have knockback
      Vec3 knockback = new Vec3(x - target.getX(), y - target.getY(), z - target.getZ());
      // goal is dividing the scale by the square root of the length, computed as the negative 4th root of the length squared to reduce sqrt calls.
      knockback = knockback.scale(strength * Math.pow(knockback.lengthSqr(), -0.25f));
      target.push(knockback.x, knockback.y, knockback.z);
      if (target instanceof ServerPlayer player) {
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
      }

      pulled++;
      if (pulled > 25) {
        break;
      }
    }
    // since we did something useful, toss the projectile even if unused
    if (pulled > 0) {
      ReusableProjectile.discard(projectile);
    }
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target, boolean notBlocked) {
    pullMobs(modifier, projectile, hit.getLocation());
    return false;
  }

  @Override
  public boolean onProjectileHitsBlock(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, BlockHitResult hit, @Nullable LivingEntity owner) {
    pullMobs(modifier, projectile, hit.getLocation());
    return false;
  }

  @Override
  public void onProjectileFuseFinish(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow) {
    pullMobs(modifier, projectile, projectile.position());
  }
}
