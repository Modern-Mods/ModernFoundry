package modernmods.modernfoundry.tools.modules.cosmetic;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IModDataView;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing {@link modernmods.modernfoundry.tools.TinkerModifiers#dyed} */
public enum DyeModule implements ModifierModule, DisplayNameModifierHook, ModifierRemovalHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<DyeModule>defaultHooks(ModifierHooks.DISPLAY_NAME, ModifierHooks.REMOVE);
  public static final RecordLoadable<DyeModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<? extends ModifierModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    IModDataView persistentData = tool.getPersistentData();
    Identifier key = entry.getId().getIdentifier();
    if (persistentData.contains(key)) {
      int color = persistentData.getInt(key);
      Modifier modifier = entry.getModifier();
      return modifier.applyStyle(Component.translatable(modifier.getTranslationKey() + ".formatted",
        Component.literal(String.format("#%06X", color)).withStyle(Style.EMPTY.withColor(color))
      ));
    }
    return name;
  }

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    tool.getPersistentData().remove(modifier.getId().getIdentifier());
    return null;
  }
}
