package modernmods.modernfoundry.integrations.items.modifiers.tool;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import modernmods.hilt.client.TooltipKey;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;

public class SirenModifier extends NoLevelsModifier implements BreakSpeedModifierHook, TooltipModifierHook, BlockInteractionModifierHook, MeleeDamageModifierHook {

    private static final float ATTACK_BONUS = 1.5F;
    private static final float SPEED_BONUS = 5.0F;
    private static final Component MINING_SPEED = Component.translatable(
            Util.makeDescriptionId("modifier", resource("siren.mining_speed")));

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BREAK_SPEED, ModifierHooks.TOOLTIP, ModifierHooks.BLOCK_INTERACT, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        if (tool.hasTag(TinkerTags.Items.MELEE) && context.getAttacker().isEyeInFluid(FluidTags.WATER) && !tool.isBroken()) {
            return damage * ATTACK_BONUS;
        }

        return damage;
    }

    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, PlayerEvent.BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        final Player player = event.getEntity();

        if (player != null && !player.level().isClientSide && !tool.isBroken()) {
            final ServerPlayer sp = (ServerPlayer) player;
            boolean isSubmerged = sp.isUnderWater() && sp.isInWater() && sp.isInWaterRainOrBubble();

            if (isEffective && tool.hasTag(TinkerTags.Items.HARVEST) && isSubmerged) {
                event.setNewSpeed(event.getNewSpeed() + (SPEED_BONUS * tool.getMultiplier(ToolStats.MINING_SPEED) * miningSpeedModifier));
            }
        }
    }

    @Override
    public InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        Player player = context.getPlayer();
        Level mcLevel = context.getLevel();

        if (player != null && !mcLevel.isClientSide && !tool.isBroken()) {
            BlockPos pos = context.getClickedPos();
            BlockState blockState = mcLevel.getBlockState(pos);
            boolean tillable = blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.DIRT_PATH) || blockState.is(Blocks.DIRT);
            var aquacultureFarmland = OptionalIntegrationHelper.block("aquaculture:farmland");

            if (tillable
                    && aquacultureFarmland != null
                    && context.getClickedFace() != Direction.DOWN
                    && mcLevel.isEmptyBlock(pos.above())
                    && blockState != null) {
                mcLevel.setBlock(pos, aquacultureFarmland.defaultBlockState(), 2);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        float bonus = 0.0F;

        if (tool.hasTag(TinkerTags.Items.MELEE)) {
            if (player != null
                    && tooltipKey == TooltipKey.SHIFT
                    && player.isEyeInFluid(FluidTags.WATER)) {
                float damage = ToolAttackUtil.getAttributeAttackDamage(tool, player, modernmods.modernfoundry.library.utils.Util.getSlotType(player.getUsedItemHand()));
                bonus = damage * ATTACK_BONUS;
            }

            if (bonus > 0.0F) {
                TooltipModifierHook.addDamageBoost(tool, modifier.getModifier(), bonus, tooltip);
            }
        }

        if (tool.hasTag(TinkerTags.Items.HARVEST)) {
            if (player != null
                    && tooltipKey == TooltipKey.SHIFT
                    && player.isEyeInFluid(FluidTags.WATER)) {
                bonus = SPEED_BONUS;
            }

            if (bonus > 0.0F) {
                TooltipModifierHook.addFlatBoost(modifier.getModifier(), MINING_SPEED, bonus * tool.getMultiplier(ToolStats.MINING_SPEED), tooltip);
            }
        }
    }

}
