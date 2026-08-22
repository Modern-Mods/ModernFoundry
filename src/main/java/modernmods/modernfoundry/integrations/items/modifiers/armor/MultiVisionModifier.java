package modernmods.modernfoundry.integrations.items.modifiers.armor;

import java.util.List;

import modernmods.hilt.data.loadable.record.SingletonLoader;

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

public enum MultiVisionModifier implements ModifierModule, RawDataModifierHook {
    INSTANCE;

    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MultiVisionModifier>defaultHooks(ModifierHooks.RAW_DATA);
    public static final SingletonLoader<MultiVisionModifier> LOADER = new SingletonLoader<>(INSTANCE);
    public static final String VOLTMETER = "ie_voltmeter";

    @Override
    public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag tag) {
        if (tool.hasTag(TinkerTags.Items.HELMETS)) {
            tag.putBoolean(VOLTMETER, true);
        }
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag tag) {
        tag.remove(VOLTMETER);
    }

    @Override
    public SingletonLoader<? extends ModifierModule> getLoader() {
        return LOADER;
    }

}


