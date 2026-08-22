package modernmods.modernfoundry.thinking.common.modifer.harvest;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
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
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import java.util.List;

public class ShadyModifier extends Modifier implements ConditionalStatModifierHook, BreakSpeedModifierHook, TooltipModifierHook {
    private static final Component MINING_SPEED = TConstruct.makeTranslation("modifier", "shady.mining_speed");
    private static final Component VELOCITY = TConstruct.makeTranslation("modifier", "shady.velocity");

    @Override
    public int getPriority() {
        // run this last as we boost original speed, adds to existing boosts
        return 75;
    }
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.CONDITIONAL_STAT, ModifierHooks.BREAK_SPEED, ModifierHooks.TOOLTIP);
    }

    @Override
    public void onBreakSpeed(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, PlayerEvent.@NotNull BreakSpeed event, @NotNull Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        if (!isEffective) {
            return;
        }
        Level world =event.getEntity().getCommandSenderWorld();
        event.setNewSpeed((float) (event.getNewSpeed()*( 1+(15-(world.getBrightness(LightLayer.SKY, event.getEntity().blockPosition())-world.getSkyDarken()))*0.015*modifier.getLevel())));
    }
    @Override
    public float modifyStat(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull LivingEntity living, @NotNull FloatToolStat stat, float baseValue, float multiplier) {
        if (stat == ToolStats.VELOCITY) {
            Level world = living.getCommandSenderWorld();
            return (float) (baseValue*( 1+(15-(world.getBrightness(LightLayer.SKY, living.blockPosition())-world.getSkyDarken()))*0.01*modifier.getLevel()));
        }
        return baseValue;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        boolean harvest = tool.hasTag(TinkerTags.Items.HARVEST);
        if ((harvest || tool.hasTag(TinkerTags.Items.RANGED))&&player!=null) {
            Component prefix = harvest ? MINING_SPEED : VELOCITY;
            Level world = player.getCommandSenderWorld();
            double boost = (15-(world.getBrightness(LightLayer.SKY, player.blockPosition())-world.getSkyDarken()))*(harvest?0.015:0.01)*modifier.getLevel();
            TooltipModifierHook.addPercentBoost(this, prefix, boost, tooltip);
        }
    }
}
