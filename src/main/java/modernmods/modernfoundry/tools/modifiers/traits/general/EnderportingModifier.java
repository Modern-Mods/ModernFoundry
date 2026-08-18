package modernmods.modernfoundry.tools.modifiers.traits.general;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.events.teleport.EnderportingTeleportEvent;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.entity.ReusableProjectile;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockHarvestModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileFuseModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.PlantHarvestModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.utils.TeleportHelper;

import javax.annotation.Nullable;

public class EnderportingModifier extends NoLevelsModifier implements PlantHarvestModifierHook, ProjectileHitModifierHook, ProjectileLaunchModifierHook, BlockHarvestModifierHook, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, ProjectileFuseModifierHook {
  private static final Identifier SECONDARY_ARROW = TConstruct.getResource("enderporting_secondary");

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.PLANT_HARVEST, ModifierHooks.PROJECTILE_HIT, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.BLOCK_HARVEST, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_FUSE);
  }

  @Override
  public int getPriority() {
    return 45;
  }

  /** Attempts to teleport to the given location */
  private static boolean tryTeleport(ModifierEntry modifier, LivingEntity living, double x, double y, double z) {
    Level world = living.level();
    // should never happen with the hooks, but just in case
    if (world.isClientSide()) {
      return false;
    }
    // this logic is cloned from suffocation damage logic
    float scaledWidth = living.getBbWidth() * 0.8F;
    float eyeHeight = living.getEyeHeight();
    AABB aabb = AABB.ofSize(new Vec3(x, y + (eyeHeight / 2), z), scaledWidth, eyeHeight, scaledWidth);

    boolean didCollide = world.getBlockCollisions(living, aabb).iterator().hasNext();

    // if we collided, try again 1 block down, means mining the top of 2 blocks is valid
    if (didCollide && living.getBbHeight() > 1) {
      // try again 1 block down
      aabb = aabb.move(0, -1, 0);
      didCollide = world.getBlockCollisions(living, aabb).iterator().hasNext();
      y -= 1;
    }

    // as long as no collision now, we can teleport
    if (!didCollide) {
      return TeleportHelper.tryTeleport(new EnderportingTeleportEvent(living, x, y, z, modifier));
    }
    return false;
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    if (!context.isExtraAttack()) {
      LivingEntity target = context.getLivingTarget();
      // if the entity is dead now
      if (target != null) {
        LivingEntity attacker = context.getAttacker();
        Vec3 oldPosition = attacker.position();
        if (tryTeleport(modifier, attacker, target.getX(), target.getY(), target.getZ())) {
          tryTeleport(modifier, target, oldPosition.x, oldPosition.y, oldPosition.z);
          ToolDamageUtil.damageAnimated(tool, 2, attacker, context.getSlotType(), modifier.getId());
        }
      }
    }
  }

  @Override
  public void finishHarvest(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context, int harvested) {
    if (harvested > 0 && context.canHarvest() && tool.hasTag(TinkerTags.Items.HARVEST)) {
      BlockPos pos = context.getPos();
      LivingEntity living = context.getLiving();
      if (tryTeleport(modifier, living, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f)) {
        ToolDamageUtil.damageAnimated(tool, 2, living, EquipmentSlot.MAINHAND, modifier.getId());
      }
    }
  }

  @Override
  public void afterHarvest(IToolStackView tool, ModifierEntry modifier, UseOnContext context, ServerLevel world, BlockState state, BlockPos pos) {
    // only teleport to the center block
    if (context.getClickedPos().equals(pos)) {
      LivingEntity living = context.getPlayer();
      if (living != null && tryTeleport(modifier, living, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f)) {
        ToolDamageUtil.damageAnimated(tool, 2, living, context.getHand(), modifier.getId());
      }
    }
  }

  /** Checks if the given projectile allows teleporting */
  private static boolean canTeleport(ModDataNBT persistentData) {
    return !persistentData.getBoolean(SECONDARY_ARROW);
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    if (attacker != null && attacker != target && canTeleport(persistentData)) {
      Entity hitEntity = hit.getEntity();
      Vec3 oldPosition = attacker.position();
      if (attacker.level() == projectile.level() && tryTeleport(modifier, attacker, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ()) && target != null) {
        if (tryTeleport(modifier, target, oldPosition.x, oldPosition.y, oldPosition.z)) {
          ModifierUtil.updateFishingRod(projectile, 10, false, modifier.getId());
        }
      }
    }
    return false;
  }

  @Override
  public void onProjectileHitBlock(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, BlockHitResult hit, @Nullable LivingEntity attacker) {
    if (attacker != null && canTeleport(persistentData)) {
      BlockPos target = hit.getBlockPos().relative(hit.getDirection());
      // attempt the teleport, if successful and the projectile is not reusable then discard it
      if (attacker.level() == projectile.level() && tryTeleport(modifier, attacker, target.getX() + 0.5f, target.getY(), target.getZ() + 0.5f)) {
        ModifierUtil.updateFishingRod(projectile, 10, true);
        ReusableProjectile.discard(projectile);
      }
    }
  }

  @Override
  public void onProjectileFuseFinish(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow) {
    if (canTeleport(persistentData) && projectile.getOwner() instanceof LivingEntity attacker) {
      // no need to discard, fuse did that for us
      if (attacker.level() == projectile.level()) {
        // teleport to the expired projectile
        Vec3 target = projectile.position();
        if (tryTeleport(modifier, attacker, target.x, target.y, target.z)) {
          ModifierUtil.updateFishingRod(projectile, 10, true);
        }
      }
    }
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (primary) {
      ToolDamageUtil.damageLauncher(tool, 10, shooter, projectile, modifier.getId());
    } else {
      persistentData.putBoolean(SECONDARY_ARROW, true);
    }
  }
}
