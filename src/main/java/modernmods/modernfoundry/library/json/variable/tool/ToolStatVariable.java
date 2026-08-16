package modernmods.modernfoundry.library.json.variable.tool;

import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

/**
 * Variable to get a stat from the tool
 */
public record ToolStatVariable(INumericToolStat<?> stat) implements ToolVariable {
  public static final RecordLoadable<ToolStatVariable> LOADER = RecordLoadable.create(ToolStats.NUMERIC_LOADER.requiredField("stat", ToolStatVariable::stat), ToolStatVariable::new);

  @Override
  public float getValue(IToolStackView tool) {
    return tool.getStats().get(stat).floatValue();
  }

  @Override
  public RecordLoadable<ToolStatVariable> getLoader() {
    return LOADER;
  }
}
