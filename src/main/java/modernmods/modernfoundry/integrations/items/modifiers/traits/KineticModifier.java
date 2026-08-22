package modernmods.modernfoundry.integrations.items.modifiers.traits;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockBreakModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class KineticModifier extends NoLevelsModifier implements MeleeHitModifierHook, BlockBreakModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void failedMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageAttempted) {
        final ServerPlayer sp = (ServerPlayer) context.getPlayerAttacker();

        if (sp != null) {
            chargeInventoryItem(sp);
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        final ServerPlayer sp = (ServerPlayer) context.getPlayerAttacker();

        if (sp != null) {
            chargeInventoryItem(sp);
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        final ServerPlayer sp = context.getPlayer();

        if (sp != null && !sp.level().isClientSide) {
            chargeInventoryItem(sp);
        }
    }

    private void chargeInventoryItem(ServerPlayer sp) {
        if (chargeItems(sp.getInventory().items)) return;

        Object curiosInventory = OptionalIntegrationHelper.unwrap(OptionalIntegrationHelper.invokeStatic(
                "mekanism.common.integration.curios.CuriosIntegration", "getCuriosInventory", sp));
        if (curiosInventory != null) {
            chargeHandler(curiosInventory);
        }
    }

    private boolean chargeItems(Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty() && provideEnergy(stack)) return true;
        }
        return false;
    }

    private boolean chargeHandler(Object itemHandler) {
        int slots = (int) OptionalIntegrationHelper.number(OptionalIntegrationHelper.invoke(itemHandler, "getSlots"), 0);
        for (int slot = 0; slot < slots; slot++) {
            Object value = OptionalIntegrationHelper.invoke(itemHandler, "getStackInSlot", slot);
            if (value instanceof ItemStack stack && !stack.isEmpty() && provideEnergy(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean provideEnergy(ItemStack stack) {
        Object energyHandler = OptionalIntegrationHelper.invokeStatic(
                "mekanism.common.integration.energy.EnergyCompatUtils", "getStrictEnergyHandler", stack);
        if (energyHandler == null) return false;

        Object energy = OptionalIntegrationHelper.invokeStatic("mekanism.api.math.FloatingLong", "create", 20.0D);
        Object action = OptionalIntegrationHelper.staticField("mekanism.api.Action", "EXECUTE");
        return energy != null && action != null
                && OptionalIntegrationHelper.invoke(energyHandler, "insertEnergy", energy, action) != null;
    }

}
