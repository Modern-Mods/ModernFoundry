package modernmods.modernfoundry.tools.modifiers.ability.tool;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ToolStatsModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public class DuelWieldingModifier extends OffhandAttackModifier implements ToolStatsModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
  }

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    // on two handed tools, take a larger hit to attack damage, smaller to attack speed
    if (context.hasTag(TinkerTags.Items.BROAD_TOOLS)) {
      ToolStats.ATTACK_DAMAGE.multiplyAll(builder, 0.7);
      ToolStats.ATTACK_SPEED.multiplyAll(builder, 0.9);
    } else {
      // on one handed tools, 80% on both
      ToolStats.ATTACK_DAMAGE.multiplyAll(builder, 0.8);
      ToolStats.ATTACK_SPEED.multiplyAll(builder, 0.8);
    }
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return true;
  }
}
