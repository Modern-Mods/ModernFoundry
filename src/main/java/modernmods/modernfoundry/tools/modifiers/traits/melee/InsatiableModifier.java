package modernmods.modernfoundry.tools.modifiers.traits.melee;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import modernmods.mantle.client.TooltipKey;
import modernmods.modernfoundry.common.TinkerEffect;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingLaunchModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.stats.ToolType;

import javax.annotation.Nullable;
import java.util.List;

public class InsatiableModifier extends Modifier implements ProjectileHitModifierHook, ConditionalStatModifierHook, MeleeDamageModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, MeleeHitModifierHook, SlingLaunchModifierHook, TooltipModifierHook {
  public static final ToolType[] TYPES = {ToolType.LAUNCHER, ToolType.MELEE};

  /** Gets the current bonus for the entity */
  private static float getEffect(LivingEntity attacker, ToolType type) {
    // TODO 1.21: switch values in enum for the effect
    if (type == ToolType.LAUNCHER) {
      type = ToolType.RANGED;
    }
    return TinkerEffect.getLevel(attacker, TinkerModifiers.insatiableEffect.get(type));
  }

  /** Applies the effect to the target */
  public static void applyEffect(LivingEntity living, ToolType type, int duration, int add, int maxLevel) {
    TinkerEffect effect = TinkerModifiers.insatiableEffect.get(type);
    effect.apply(living, duration, Math.min(maxLevel, TinkerEffect.getAmplifier(living, effect) + add), true);
  }

  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT, ModifierHooks.CONDITIONAL_STAT, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MONSTER_MELEE_DAMAGE, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.SLING_LAUNCH, ModifierHooks.TOOLTIP);
  }

  @Override
  public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
    // gives +0.5 per effect level, for +2.5 per modifier level at max
    return damage + (getEffect(context.getAttacker(), ToolType.MELEE) * modifier.getEffectiveLevel() / 2f * tool.getMultiplier(ToolStats.ATTACK_DAMAGE));
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    // 5 hits gets you to max
    if (!context.isExtraAttack() && context.isFullyCharged()) {
      applyEffect(context.getAttacker(), ToolType.MELEE, 5*20, 1, 4);
    }
  }

  @Override
  public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
    if (stat == ToolStats.PROJECTILE_DAMAGE) {
      // gives +0.25 power per effect level, up to +1.25 at 5 levels
      baseValue += (getEffect(living, ToolType.RANGED) * modifier.getEffectiveLevel() / 4f * multiplier);
    }
    return baseValue;
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target, boolean notBlocked) {
    if (notBlocked && attacker != null) {
      applyEffect(attacker, ToolType.RANGED, 10*20, 1, 4);
    }
    return false;
  }

  @Override
  public void afterSlingLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity holder, LivingEntity target, ModifierEntry slingSource, float force, float multiplier, Vec3 angle) {
    // if the sling launches, it hits
    applyEffect(holder, ToolType.RANGED, 10*20, 1, 4);
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag) {
    // run both tooltips always, helps for ranged weapons to show the right bonus
    for (ToolType type : TYPES) {
      if (tool.hasTag(type.getTag())) {
        float level = modifier.getEffectiveLevel();
        float bonus = level * 2.5f;
        // if not in the tinker station, show the realized bonus instead of the max bonus
        if (player != null && key == TooltipKey.SHIFT) {
          bonus = getEffect(player, type) * level / 2;
        }
        if (bonus > 0) {
          INumericToolStat<?> stat = type == ToolType.MELEE ? ToolStats.ATTACK_DAMAGE : ToolStats.PROJECTILE_DAMAGE;
          bonus *= tool.getMultiplier(stat);
          // ranged gets half the bonus of melee
          if (type == ToolType.LAUNCHER) {
            bonus /= 2;
          }
          TooltipModifierHook.addFlatBoost(this, TooltipModifierHook.statName(this, stat), bonus, tooltip);
        }
      }
    }
  }
}
