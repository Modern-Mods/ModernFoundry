package modernmods.modernfoundry.library.tools.definition.module.display;

import modernmods.mantle.data.loadable.mapping.SimpleRecordLoadable;
import modernmods.mantle.data.loadable.primitive.EnumLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;

import java.util.List;

/** Module setting the tool display name to a filtered list of material stat types */
public enum MaterialToolNameModule implements MaterialToolName, ToolModule {
  ALL {
    @Override
    public boolean shouldDisplayMaterial(int index, MaterialStatsId statType, MaterialVariantId material) {
      return true;
    }
  },
  REPAIRABLE {
    @Override
    public boolean shouldDisplayMaterial(int index, MaterialStatsId statType, MaterialVariantId material) {
      return MaterialRegistry.getInstance().canRepair(statType);
    }
  };
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaterialToolNameModule>defaultHooks(ToolHooks.DISPLAY_NAME);
  public static final RecordLoadable<MaterialToolNameModule> LOADER = new SimpleRecordLoadable<>(new EnumLoadable<>(MaterialToolNameModule.class), "filter", null, false);

  @Override
  public RecordLoadable<MaterialToolNameModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
