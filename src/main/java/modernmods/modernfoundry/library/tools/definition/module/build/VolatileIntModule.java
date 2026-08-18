package modernmods.modernfoundry.library.tools.definition.module.build;

import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.ToolDataNBT;

import java.util.List;

/**
 * Module that just sets a boolean flag to true on a tool.
 * @see VolatileFlagModule
 * @see modernmods.modernfoundry.library.modifiers.modules.build.VolatileIntModule
 */
public record VolatileIntModule(Identifier flag, int value) implements ToolModule, VolatileDataToolHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileIntModule>defaultHooks(ToolHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileIntModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileIntModule::flag),
    IntLoadable.ANY_FULL.requiredField("value", VolatileIntModule::value),
    VolatileIntModule::new);

  @Override
  public RecordLoadable<VolatileIntModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(IToolContext context, ToolDataNBT volatileData) {
    volatileData.putInt(flag, volatileData.getInt(flag) + value);
  }
}
