package modernmods.modernfoundry.integrations.items.modifiers.traits;

import net.minecraft.server.level.ServerPlayer;

import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockBreakModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/*
 * This should be mod independent. Just increases AirSupply by 20 if mining with an Oxygenated tool.
 */

public class OxygenatedModifier extends Modifier implements BlockBreakModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        final ServerPlayer sp = context.getPlayer();

        if (sp != null && !sp.level().isClientSide) {
            sp.setAirSupply(Math.min(sp.getMaxAirSupply(), sp.getAirSupply() + 20));
        }
    }

}


