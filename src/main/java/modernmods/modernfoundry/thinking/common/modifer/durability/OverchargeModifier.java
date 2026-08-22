package modernmods.modernfoundry.thinking.common.modifer.durability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ToolStatsModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.capability.ToolEnergyCapability;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;

import static modernmods.modernfoundry.library.tools.capability.ToolEnergyCapability.ENERGY_HANDLER;
import static modernmods.modernfoundry.library.tools.capability.ToolEnergyCapability.MAX_STAT;

public class OverchargeModifier extends Modifier implements InventoryTickModifierHook, ToolStatsModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.TOOL_STATS).addModule(ENERGY_HANDLER);
    }
    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        MAX_STAT.add(builder, builder.getStat(OverslimeModule.OVERSLIME_STAT) * 100);
    }
    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Level world, @NotNull LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        // update 1 times a second, but skip when active (messes with pulling bow back)
        if (!world.isClientSide && holder.tickCount % 20 == 0 && holder.getUseItem() != stack) {
            // ensure we have overslime
            int x =ToolEnergyCapability.getEnergy(tool);
            if (OverslimeModule.getCapacity(tool) > OverslimeModule.INSTANCE.getAmount(tool) && x >= 200) {
                int y = Math.min(x / 200, modifier.getLevel());
                OverslimeModule.INSTANCE.addAmount(tool, modifier, y);
                ToolEnergyCapability.setEnergy(tool,  ToolEnergyCapability.getEnergy(tool) - 200 * y);
            }
        }
    }
}
