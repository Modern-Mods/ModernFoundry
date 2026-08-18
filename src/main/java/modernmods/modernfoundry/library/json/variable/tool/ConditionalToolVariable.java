package modernmods.modernfoundry.library.json.variable.tool;

import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.json.predicate.tool.ToolContextPredicate;
import modernmods.modernfoundry.library.json.variable.ConditionalVariable;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/**
 * Gets one of two entity properties based on the condition
 */
public record ConditionalToolVariable(IJsonPredicate<IToolContext> condition, ToolVariable ifTrue, ToolVariable ifFalse) implements ToolVariable, ConditionalVariable<IJsonPredicate<IToolContext>,ToolVariable> {
  public static final RecordLoadable<ConditionalToolVariable> LOADER = ConditionalVariable.loadable(ToolContextPredicate.LOADER, ToolVariable.LOADER, ConditionalToolVariable::new);

  public ConditionalToolVariable(IJsonPredicate<IToolContext> condition, float ifTrue, float ifFalse) {
    this(condition, new ToolVariable.Constant(ifTrue), new ToolVariable.Constant(ifFalse));
  }

  @Override
  public float getValue(IToolStackView tool) {
    return condition.matches(tool) ? ifTrue.getValue(tool) : ifFalse.getValue(tool);
  }

  @Override
  public RecordLoadable<ConditionalToolVariable> getLoader() {
    return LOADER;
  }
}
