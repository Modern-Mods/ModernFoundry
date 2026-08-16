package modernmods.modernfoundry.library.modifiers.modules.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/** Module to show the interaction source in the modifier name. Used to prevent showing multiple times when multiple sources are combined. */
public enum ShowInteractionSourceModule implements ModifierModule, DisplayNameModifierHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ShowInteractionSourceModule>defaultHooks(ModifierHooks.DISPLAY_NAME);
  public static final RecordLoadable<ShowInteractionSourceModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<ShowInteractionSourceModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    return InteractionSource.formatModifierName(tool, entry.getModifier(), name);
  }

}
