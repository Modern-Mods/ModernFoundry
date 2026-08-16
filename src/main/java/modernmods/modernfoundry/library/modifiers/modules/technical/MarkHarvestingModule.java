package modernmods.modernfoundry.library.modifiers.modules.technical;

import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockHarvestModifierHook;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;

import java.util.List;

/** Simple module with hooks form of {@link BlockHarvestModifierHook.MarkHarvesting}. */
public enum MarkHarvestingModule implements BlockHarvestModifierHook.MarkHarvesting, HookProvider {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MarkHarvestingModule>defaultHooks(ModifierHooks.BLOCK_HARVEST);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
