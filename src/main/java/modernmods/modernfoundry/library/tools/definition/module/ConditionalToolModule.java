package modernmods.modernfoundry.library.tools.definition.module;

import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.hilt.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.ModuleHook;

import java.util.List;

/**
 * Datagen helper for making conditional {@link ModifierModule}.
 * @param ifTrue      Module to use if all conditions are true.
 * @param ifFalse     Module to use if any condition is false.
 * @param conditions  Conditions to evaluate.
 */
@SuppressWarnings("unused") // API
public record ConditionalToolModule(ToolModule ifTrue, ToolModule ifFalse, ICondition... conditions) implements ToolModule, ConditionalObject<ToolModule> {
  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return List.of();
  }

  @Override
  public RecordLoadable<? extends ToolModule> getLoader() {
    return ToolModule.LOADER.getConditionalLoader();
  }
}
