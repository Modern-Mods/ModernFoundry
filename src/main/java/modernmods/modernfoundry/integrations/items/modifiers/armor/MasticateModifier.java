package modernmods.modernfoundry.integrations.items.modifiers.armor;

import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.module.ModuleHookMap;

import modernmods.modernfoundry.tools.modules.armor.ThornsModule;

public class MasticateModifier extends Modifier {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addModule(ThornsModule.builder().constantFlat(2).randomFlat(3).build());
    }

}


