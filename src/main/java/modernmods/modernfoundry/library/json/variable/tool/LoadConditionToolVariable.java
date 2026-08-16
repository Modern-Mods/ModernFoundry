package modernmods.modernfoundry.library.json.variable.tool;

import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.hilt.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

/**
 * Datagen helper for making conditional {@link ToolVariable}.
 * @param ifTrue      Variable to use if all conditions are true.
 * @param ifFalse     Variable to use if any condition is false.
 * @param conditions  Conditions to evaluate.
 */
@SuppressWarnings("unused") // API
public record LoadConditionToolVariable(ToolVariable ifTrue, ToolVariable ifFalse, ICondition... conditions) implements ToolVariable, ConditionalObject<ToolVariable> {
  @Override
  public float getValue(IToolStackView tool) {
    return (Util.testConditions(conditions) ? ifTrue : ifFalse).getValue(tool);
  }

  @Override
  public RecordLoadable<? extends ToolVariable> getLoader() {
    return ToolVariable.LOADER.getConditionalLoader();
  }
}
