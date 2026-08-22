package modernmods.modernfoundry.integrations.items;

import java.util.Collection;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.module.ModuleHook;

import modernmods.modernfoundry.integrations.items.modifiers.hooks.IArmorCrouchModifier;
import modernmods.modernfoundry.integrations.items.modifiers.hooks.IArmorJumpModifier;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;

public class TciIntegrationHooks {

    public static ModuleHook<IArmorCrouchModifier> CROUCH;
    public static ModuleHook<IArmorJumpModifier> JUMP;

    private TciIntegrationHooks() {}

    public static void init() {
        CROUCH = register("crouch", IArmorCrouchModifier.class, IArmorCrouchModifier.AllMerger::new, new IArmorCrouchModifier() {});
        JUMP = register("jump", IArmorJumpModifier.class, IArmorJumpModifier.ALL_MERGER, (tool, living) -> {});
    }

    private static <T> ModuleHook<T> register(String name, Class<T> filter, @Nullable Function<Collection<T>,T> merger, T defaultInstance) {
        return ModifierHooks.register(resource(name), filter, merger, defaultInstance);
    }

}
