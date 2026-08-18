package modernmods.modernfoundry.tools.modifiers.traits.melee;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import modernmods.mantle.util.CombatHelper;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.ArmorLootingModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.LootingModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.LootingContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.DummyToolStack;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.modules.armor.CounterModule;

import javax.annotation.Nullable;

public class LaceratingModifier extends Modifier implements ProjectileHitModifierHook, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, OnAttackedModifierHook {
  /** Applies the effect to the target */
  private static void applyEffect(IToolStackView tool, @Nullable LivingEntity holder, LivingEntity target, int level, @Nullable EquipmentSlot slot) {
    // determine looting
    int looting = 0;
    DamageSource source = CombatHelper.damageSource(TinkerDamageTypes.BLEEDING, target);
    if (holder != null) {
      LootingContext context = new LootingContext(holder, target, source, slot);
      // fetch slot looting for the melee hook
      looting = LootingModifierHook.getLooting(tool, context, looting);
      // fetch armor looting
      looting = ArmorLootingModifierHook.getLooting(null, context, looting);
    }

    // 81 ticks will do about 4 damage
    applyEffect(target, level, looting, holder);
  }

  /** Applies the effect to the target */
  private static void applyEffect(LivingEntity target, int level, int looting, @Nullable Entity cause) {
    int duration = level * 2 * 20;
    MobEffectInstance existing = target.getEffect(TinkerEffects.holder(TinkerEffects.bleeding));
    if (existing != null && existing.getAmplifier() == looting) {
      duration += existing.getDuration();
    } else {
      // add a small delay so first damage happens in about a second
      // skip when already present so we continue on the same clock and don't repeat a damage
      duration += 19;
    }
    target.addEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.bleeding), duration, looting), cause);
  }


    @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.ON_ATTACKED);
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    // 50% chance of applying
    LivingEntity target = context.getLivingTarget();
    if (target != null && context.isFullyCharged() && target.isAlive()) {
      // set entity so the potion is attributed as a player kill
      target.setLastHurtMob(context.getAttacker());
      applyEffect(tool, context.getAttacker(), target, modifier.getLevel(), context.getSlotType());
    }
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    if (target != null && (!(projectile instanceof AbstractArrow arrow) || arrow.isCritArrow()) && target.isAlive()) {
      Entity owner = projectile.getOwner();
      if (owner != null) {
        target.setLastHurtMob(owner);
      }
      // can only use looting hook if we have an attacker living
      if (attacker != null) {
        applyEffect(new DummyToolStack(Items.AIR, modifiers, persistentData), attacker, target, modifier.getLevel(), null);
      } else {
        applyEffect(target, modifier.getLevel(), 0, projectile.getEffectSource());
      }
    }
    return false;
  }

  @Override
  public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    // this works like vanilla, damage is capped due to the hurt immunity mechanics, so if multiple pieces apply thorns between us and vanilla, damage is capped at 4
    if (isDirectDamage && tool.hasTag(TinkerTags.Items.ARMOR) && source.getEntity() instanceof LivingEntity attacker) {
      // 25% chance of working, doubled chance on shields
      float chance = 0.25f;
      if (CounterModule.isBlocking(tool, slotType, context.getEntity())) {
        chance *= 2;
      }
      if (RANDOM.nextFloat() < chance) {
        applyEffect(tool, context.getEntity(), attacker, modifier.getLevel(), slotType);
      }
    }
  }
}
