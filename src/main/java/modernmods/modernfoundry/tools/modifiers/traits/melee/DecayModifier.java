package modernmods.modernfoundry.tools.modifiers.traits.melee;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.tools.modules.armor.CounterModule;

import javax.annotation.Nullable;

/** @deprecated use {@link modernmods.modernfoundry.library.modifiers.modules.combat.MobEffectModule} with its various forms. */
@Deprecated(forRemoval = true)
public class DecayModifier extends Modifier implements ProjectileLaunchModifierHook, ProjectileHitModifierHook, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, OnAttackedModifierHook {
  /* gets the effect for the given level, including a random time */
  private static MobEffectInstance makeDecayEffect(int level) {
    // potions are 0 indexed instead of 1 indexed
    // wither skeletons apply 10 seconds of wither for comparison
    return new MobEffectInstance(MobEffects.WITHER, 20 * (5 + (RANDOM.nextInt(level * 3))), level - 1);
  }

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_HIT, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.ON_ATTACKED);
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    if (context.isFullyCharged()) {
      // note the time of each effect is calculated independently

      // 25% chance to poison yourself
      LivingEntity attacker = context.getAttacker();
      if (RANDOM.nextInt(3) == 0) {
        attacker.addEffect(makeDecayEffect(modifier.getLevel()));
      }

      // always poison the target, means it works twice as often as lacerating
      LivingEntity target = context.getLivingTarget();
      if (target != null && target.isAlive()) {
        target.addEffect(makeDecayEffect(modifier.getLevel()), attacker);
      }
    }
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    if (target != null && (!(projectile instanceof AbstractArrow arrow) || arrow.isCritArrow())) {
      // always poison the target, means it works twice as often as lacerating
      target.addEffect(makeDecayEffect(modifier.getLevel()), projectile.getEffectSource());
    }
    return false;
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (primary && (arrow == null || arrow.isCritArrow()) && RANDOM.nextInt(3) == 0) {
      // 25% chance to poison yourself
      shooter.addEffect(makeDecayEffect(modifier.getLevel()));
    }
  }

  @Override
  public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    // this works like vanilla, effect is capped due to not being able to receive multiple instances
    if (isDirectDamage && tool.hasTag(TinkerTags.Items.ARMOR) && source.getEntity() instanceof LivingEntity attacker) {
      // 10% chance to poison yourself
      LivingEntity defender = context.getEntity();
      // 50% chance of working, doubled bonus on shields. Makes it twice lacerating
      float chance = 0.5f;
      if (CounterModule.isBlocking(tool, slotType, context.getEntity())) {
        chance *= 2;
      }
      if (chance >= 1 || RANDOM.nextFloat() < chance) {
        attacker.addEffect(makeDecayEffect(modifier.getLevel()), defender);
      }

      // 10% chance of poisoning you too, independently generated time
      if (RANDOM.nextInt(10) == 0) {
        defender.addEffect(makeDecayEffect(modifier.getLevel()));
      }
    }
  }
}
