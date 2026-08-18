package modernmods.modernfoundry.library.modifiers.modules.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import modernmods.mantle.client.ResourceColorManager;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that colors the display name based on the variant.
 * @see ModifierVariantNameModule
 * @see MaterialVariantColorModule
 */
public enum ModifierVariantColorModule implements ModifierModule, DisplayNameModifierHook {
  INSTANCE;

  public static final RecordLoadable<ModifierVariantColorModule> LOADER = new SingletonLoader<>(INSTANCE);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierVariantColorModule>defaultHooks(ModifierHooks.DISPLAY_NAME);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ModifierVariantColorModule> getLoader() {
    return LOADER;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    String variant = tool.getPersistentData().getString(entry.getId().getIdentifier());
    if (!variant.isEmpty()) {
      String key = entry.getModifier().getTranslationKey();
      return name.copy().withStyle(style -> style.withColor(ResourceColorManager.getTextColor(key + "." + variant)));
    }
    return name;
  }
}
