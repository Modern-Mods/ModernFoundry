package modernmods.modernfoundry.library.tools.definition.module;

import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.mantle.data.registry.GenericLoaderRegistry;
import modernmods.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.WithHooks;

import java.util.List;

/**
 * Base interface for modules within the tool definition data
 */
public interface ToolModule extends IHaveLoader, HookProvider {
  /** Empty module instance. Used as fallback for {@link ConditionalToolModule} modules. */
  ToolModule EMPTY = SingletonLoader.singleton(loader -> new ToolModule() {
    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
      return List.of();
    }

    @Override
    public RecordLoadable<? extends ToolModule> getLoader() {
      return loader;
    }
  });
  /** Loader instance for any modules loadable in tools */
  GenericLoaderRegistry<ToolModule> LOADER = new GenericLoaderRegistry<>("Tool Module", EMPTY, false);
  /** Loadable for modules including hooks */
  RecordLoadable<WithHooks<ToolModule>> WITH_HOOKS = WithHooks.makeLoadable(LOADER, ToolHooks.LOADER);

  @Override
  RecordLoadable<? extends ToolModule> getLoader();
}
