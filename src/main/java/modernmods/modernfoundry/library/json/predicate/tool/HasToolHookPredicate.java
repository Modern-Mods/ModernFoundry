package modernmods.modernfoundry.library.json.predicate.tool;

import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;

/** Predicate matching tools with the given hook */
public record HasToolHookPredicate(ModuleHook<?> hook) implements ToolContextPredicate {
  public static final RecordLoadable<HasToolHookPredicate> LOADER = RecordLoadable.create(ToolHooks.LOADER.requiredField("hook", HasToolHookPredicate::hook), HasToolHookPredicate::new);

  @Override
  public boolean matches(IToolContext tool) {
    return tool.getDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public RecordLoadable<? extends IJsonPredicate<IToolContext>> getLoader() {
    return LOADER;
  }
}
