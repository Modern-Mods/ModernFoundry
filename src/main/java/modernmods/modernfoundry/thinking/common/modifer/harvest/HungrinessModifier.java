package modernmods.modernfoundry.thinking.common.modifer.harvest;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeModule;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import java.util.List;

public class HungrinessModifier extends Modifier implements ConditionalStatModifierHook, BreakSpeedModifierHook,TooltipModifierHook{
    private static final Component MINING_SPEED = TConstruct.makeTranslation("modifier", "hungriness.mining_speed");
    private static final Component VELOCITY = TConstruct.makeTranslation("modifier", "hungriness.velocity");
    @Override
    public int getPriority() {
        return 55;
    }
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.CONDITIONAL_STAT,ModifierHooks.BREAK_SPEED,ModifierHooks.TOOLTIP);
    }

    @Override
    public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
        if (stat == ToolStats.VELOCITY&&living instanceof Player) {
            return (float) (baseValue*(1+(20-(((Player)living).getFoodData().getFoodLevel()))*0.02*modifier.getLevel()));
        }
        return baseValue;
    }
    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, PlayerEvent.@NotNull BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        if (!isEffective||event.getEntity()==null) {
            return;
        }
        event.setNewSpeed((float) (event.getNewSpeed()*(1+(20-(event.getEntity().getFoodData().getFoodLevel()))*0.02*modifier.getLevel())));
    }
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        boolean harvest = tool.hasTag(TinkerTags.Items.HARVEST);
        float boost;
        if ((harvest || tool.hasTag(TinkerTags.Items.RANGED))&&player!=null) {
            Component prefix = harvest ? MINING_SPEED : VELOCITY;
            boost = (float) ((20 - (player.getFoodData().getFoodLevel())) * 0.02 * modifier.getLevel());
            TooltipModifierHook.addPercentBoost(this, prefix, boost, tooltip);
        }
    }
}
