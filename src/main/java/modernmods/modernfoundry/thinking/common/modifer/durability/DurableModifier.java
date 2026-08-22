package modernmods.modernfoundry.thinking.common.modifer.durability;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import java.util.List;

public class DurableModifier extends Modifier implements ToolDamageModifierHook, TooltipModifierHook {
    private static final Component prefix = TConstruct.makeTranslation("modifier", "durable.chance");
    @Override
    public int getPriority() {
        return 200; // after , before
    }
    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE,ModifierHooks.TOOLTIP);
    }
    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        float chance = (float) Math.min(Math.pow(modifier.getLevel(),0.5)*5*Math.pow(tool.getDamage()/tool.getStats().get(ToolStats.DURABILITY),2),0.9);
        int maxDamage = amount;
        // for each damage we will take, if the random number is below chance, reduce
        for (int i = 0; i < maxDamage; i++) {
            if (RANDOM.nextFloat() < chance) {
                amount--;
            }
        }
        return amount;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        if (player != null) {
            float chance = (float) Math.min(Math.pow(modifier.getLevel(),0.5)*5*Math.pow(tool.getDamage()/tool.getStats().get(ToolStats.DURABILITY),2),0.9);
            TooltipModifierHook.addPercentBoost(this, prefix, chance, tooltip);
        }
    }
}