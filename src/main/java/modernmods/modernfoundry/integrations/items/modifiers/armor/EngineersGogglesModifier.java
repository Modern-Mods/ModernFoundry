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

public enum EngineersGogglesModifier implements ModifierModule, RawDataModifierHook {
    INSTANCE;

    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<EngineersGogglesModifier>defaultHooks(ModifierHooks.RAW_DATA);
    public static final SingletonLoader<EngineersGogglesModifier> LOADER = new SingletonLoader<>(INSTANCE);
    public static final String CREATE_GOGGLES = "create_goggles";

    @Override
    public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag tag) {
        if (tool.hasTag(TinkerTags.Items.HELMETS)) {
            tag.putBoolean(CREATE_GOGGLES, true);
        }
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag tag) {
        tag.remove(CREATE_GOGGLES);
    }

    @Override
    public SingletonLoader<? extends ModifierModule> getLoader() {
        return LOADER;
    }

}


