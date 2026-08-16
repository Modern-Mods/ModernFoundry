package modernmods.modernfoundry.library.tools.definition.module.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.item.IModifiable;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Deals damage in a circle around the primary target */
public record CircleWeaponAttack(LevelingValue diameter) implements MeleeHitToolHook, ToolModule {
  public static final RecordLoadable<CircleWeaponAttack> LOADER = RecordLoadable.create(LevelingValue.ADD_TO_LEVEL.defaultField("diameter", LevelingValue.LEVEL, true, CircleWeaponAttack::diameter), CircleWeaponAttack::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<CircleWeaponAttack>defaultHooks(ToolHooks.MELEE_HIT);

  public CircleWeaponAttack(float diameter) {
    this(new LevelingValue(diameter, 1));
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<CircleWeaponAttack> getLoader() {
    return LOADER;
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ToolAttackContext context, float damage) {
    if (context.isExtraAttack()) return;
    // no need for fully charged for scythe sweep, easier than sword sweep
    // basically sword sweep logic, just deals full damage to all entities (and full effects)
    // but also takes more durability loss
    double range = diameter.compute(tool.getVolatileData().getInt(IModifiable.EXPANDED));
    // allow having no range until modified with range
    if (range > 0) {
      double rangeSq = range * range;
      LivingEntity attacker = context.getAttacker();
      Entity target = context.getTarget();
      Level level = attacker.level();
      for (LivingEntity aoeTarget : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(range, 0.25D, range))) {
        if (tool.isBroken()) {
          break;
        }
        if (aoeTarget != attacker && aoeTarget != target && !attacker.isAlliedTo(aoeTarget) && ToolAttackUtil.isAttackable(attacker, aoeTarget)
            && !(aoeTarget instanceof ArmorStand stand && stand.isMarker()) && target.distanceToSqr(aoeTarget) < rangeSq) {
          float angle = attacker.getYRot() * ((float)Math.PI / 180F);
          aoeTarget.knockback(0.4F, Mth.sin(angle), -Mth.cos(angle));
          // TODO: do we want to bring back the behavior where circle returns success if any AOE target is hit?
          ToolAttackUtil.performAttack(tool, context.withAOETarget(aoeTarget));
        }
      }

      level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F);
      Player player = context.getPlayerAttacker();
      if (!context.isProjectile() && player != null) {
        player.sweepAttack();
      }
    }
  }
}
