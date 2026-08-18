package modernmods.modernfoundry.library.tools.definition;

import lombok.Getter;
import modernmods.mantle.data.loadable.ErrorFactory;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.module.WithHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;

import java.util.List;

/**
 * This class contains all data pack configurable data for a tool, before materials are factored in.
 * Contains info about how to craft a tool and how it behaves.
 */
public class ToolDefinitionData {
  /** Empty tool data definition instance */
  public static final ToolDefinitionData EMPTY = new ToolDefinitionData(List.of(), ErrorFactory.RUNTIME);
  /** Loadable to parse definition data from JSON */
  public static final RecordLoadable<ToolDefinitionData> LOADABLE = RecordLoadable.create(ToolModule.WITH_HOOKS.list(0).defaultField("modules", List.of(), d -> d.modules), ErrorFactory.FIELD, ToolDefinitionData::new);

  private final List<WithHooks<ToolModule>> modules;
  @Getter
  private final transient ModuleHookMap hooks;

  protected ToolDefinitionData(List<WithHooks<ToolModule>> modules, ErrorFactory error) {
    this.modules = modules;
    this.hooks = ModuleHookMap.createMap(modules, error);
  }


  /* Getters */

  /** Gets the given module from the tool */
  public <T> T getHook(ModuleHook<T> hook) {
    return hooks.getOrDefault(hook);
  }
}
