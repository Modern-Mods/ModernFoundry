package modernmods.modernfoundry.library.tools.definition.module.interaction;

import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module that makes all applicable interaction modifiers run on left. Used by shields notably. Redundant on bows since they don't even call right click interaction hooks. */
public enum AttackInteraction implements InteractionToolModule, ToolModule {
  /** Singleton instance */
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<AttackInteraction>defaultHooks(ToolHooks.INTERACTION);
  /** Loader instance */
  public static final SingletonLoader<AttackInteraction> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public boolean canInteract(IToolStackView tool, ModifierId modifier, InteractionSource source) {
    return source == InteractionSource.LEFT_CLICK;
  }

  @Override
  public SingletonLoader<AttackInteraction> getLoader() {
    return LOADER;
  }
}
