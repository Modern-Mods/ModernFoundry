package modernmods.modernfoundry.library.modifiers.modules.display;

import modernmods.hilt.data.loadable.common.ColorLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Simple module to change the color of the durability bar.
 * If you have a usecase of something more complex in JSON, feel free to request it, but for now just programming what we use.
 */
public record DurabilityBarColorModule(int color) implements DurabilityDisplayModifierHook, ModifierModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<DurabilityBarColorModule>defaultHooks(ModifierHooks.DURABILITY_DISPLAY);
  public static final RecordLoadable<DurabilityBarColorModule> LOADER = RecordLoadable.create(ColorLoadable.NO_ALPHA.requiredField("color", DurabilityBarColorModule::color), DurabilityBarColorModule::new);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
  @Nullable
  @Override
  public Boolean showDurabilityBar(IToolStackView tool, ModifierEntry modifier) {
    return null; // null means no change
  }

  @Override
  public int getDurabilityWidth(IToolStackView tool, ModifierEntry modifier) {
    return 0; // 0 means no change
  }

  @Override
  public int getDurabilityRGB(IToolStackView tool, ModifierEntry modifier) {
    return color;
  }

  @Override
  public RecordLoadable<DurabilityBarColorModule> getLoader() {
    return LOADER;
  }
}
