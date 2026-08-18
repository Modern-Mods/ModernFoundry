package modernmods.modernfoundry.tools.modifiers.upgrades.melee;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import modernmods.mantle.client.TooltipKey;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ToolStatsModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/** @deprecated use {@link modernmods.modernfoundry.shared.TinkerEffects#pierce} or {@link modernmods.modernfoundry.tools.data.ModifierIds#spilling} */
@Deprecated(forRemoval = true)
public class PiercingModifier extends Modifier implements ToolStatsModifierHook, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, TooltipModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.TOOL_STATS, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.TOOLTIP);
  }

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    ToolStats.ATTACK_DAMAGE.add(builder, -0.5f * modifier.getEffectiveLevel());
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    DamageSource source = TinkerDamageTypes.source(
      context.getLevel().registryAccess(),
      TinkerDamageTypes.PIERCING,
      Objects.requireNonNullElse(context.getPlayerAttacker(), context.getAttacker()));
    // deals 0.5 pierce damage per level, scaled, half of sharpness
    float secondaryDamage = (modifier.getEffectiveLevel() * tool.getMultiplier(ToolStats.ATTACK_DAMAGE)) * context.getCooldown();
    if (context.isCritical()) {
      secondaryDamage *= 1.5f;
    }
    ToolAttackUtil.attackEntitySecondary(source, secondaryDamage, context.getTarget(), context.getLivingTarget(), true);
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    TooltipModifierHook.addDamageBoost(tool, this, modifier.getEffectiveLevel(), tooltip);
  }
}
