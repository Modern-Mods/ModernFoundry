package modernmods.modernfoundry.library.json.variable.tool;

import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

/**
 * Variable to get a stat from the tool
 */
public record StatMultiplierVariable(INumericToolStat<?> stat) implements ToolVariable {
  public static final RecordLoadable<StatMultiplierVariable> LOADER = RecordLoadable.create(ToolStats.NUMERIC_LOADER.requiredField("stat", StatMultiplierVariable::stat), StatMultiplierVariable::new);

  @Override
  public float getValue(IToolStackView tool) {
    return tool.getMultiplier(stat);
  }

  @Override
  public RecordLoadable<StatMultiplierVariable> getLoader() {
    return LOADER;
  }
}
