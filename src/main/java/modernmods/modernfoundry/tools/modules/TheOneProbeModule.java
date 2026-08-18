package modernmods.modernfoundry.tools.modules;

import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.RawDataModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.RestrictedCompoundTag;

import java.util.List;

/** Module implementing the one probe on held tools and helmets */
public enum TheOneProbeModule implements ModifierModule, RawDataModifierHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<TheOneProbeModule>defaultHooks(ModifierHooks.RAW_DATA);
  public static final SingletonLoader<TheOneProbeModule> LOADER = new SingletonLoader<>(INSTANCE);
  public static final String TOP_NBT_HELMET = "theoneprobe";
  public static final String TOP_NBT_HAND = "theoneprobe_hand";

  @Override
  public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag tag) {
    if (tool.hasTag(TinkerTags.Items.HELD)) {
      tag.putBoolean(TOP_NBT_HAND, true);
    }
    if (tool.hasTag(TinkerTags.Items.HELMETS)) {
      tag.putBoolean(TOP_NBT_HELMET, true);
    }
  }

  @Override
  public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag tag) {
    tag.remove(TOP_NBT_HAND);
    tag.remove(TOP_NBT_HELMET);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public SingletonLoader<TheOneProbeModule> getLoader() {
    return LOADER;
  }
}
