package modernmods.modernfoundry.integrations.items.modifiers.tool;

import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modifiers.ability.tool.OffhandAttackModifier;

public class MechanicalArmModifier extends OffhandAttackModifier {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
    }

    @Override
    public boolean shouldDisplay(boolean advanced) {
        return true;
    }

}


