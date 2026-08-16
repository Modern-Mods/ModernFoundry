package modernmods.modernfoundry.test;

import net.minecraft.world.item.Items;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.ToolDefinitionData;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.nbt.DummyToolStack;
import modernmods.modernfoundry.library.tools.nbt.MaterialNBT;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.tools.nbt.MultiplierNBT;
import modernmods.modernfoundry.library.tools.nbt.StatsNBT;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;

import java.util.List;

/** Helpers for running tests */
public class TestHelper {
  private TestHelper() {}

  /** Helper to fetch traits from the trait hook */
  public static List<ModifierEntry> getTraits(ToolDefinitionData data) {
    ModifierNBT.Builder builder = ModifierNBT.builder();
    data.getHook(ToolHooks.TOOL_TRAITS).addTraits(ToolDefinition.EMPTY, MaterialNBT.EMPTY, builder);
    return builder.build().getModifiers();
  }

  public record ToolDefinitionStats(StatsNBT base, MultiplierNBT multipliers) {}

  /** Computes the stats for the given tool */
  public static ToolDefinitionStats buildStats(ToolDefinitionData data) {
    ModifierStatsBuilder builder = ModifierStatsBuilder.builder();
    data.getHook(ToolHooks.TOOL_STATS).addToolStats(new DummyToolStack(Items.AIR, ModifierNBT.EMPTY, new ModDataNBT()), builder);
    MultiplierNBT multipliers = builder.buildMultipliers();
    // cancel out multipliers on the base stats, as people expect base stats to be comparable to be usable in the modifier stats builder
    for (INumericToolStat<?> stat : multipliers.getContainedStats()) {
      stat.multiply(builder, 1 / multipliers.get(stat));
    }
    return new ToolDefinitionStats(builder.build(), multipliers);
  }
}
