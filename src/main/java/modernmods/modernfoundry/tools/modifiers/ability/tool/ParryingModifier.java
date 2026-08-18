package modernmods.modernfoundry.tools.modifiers.ability.tool;
import modernmods.modernfoundry.tools.TinkerToolActions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolActionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public class ParryingModifier extends OffhandAttackModifier implements ToolActionModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.TOOL_ACTION);
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return true;
  }

  @Override
  public InteractionResult beforeEntityUse(IToolStackView tool, ModifierEntry modifier, Player player, Entity target, InteractionHand hand, InteractionSource source) {
    if (source == InteractionSource.RIGHT_CLICK) {
      InteractionResult result = super.beforeEntityUse(tool, modifier, player, target, hand, source);
      if (result.consumesAction()) {
        GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
      }
      return result;
    }
    return InteractionResult.PASS;
  }

  @Override
  public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
    if (source == InteractionSource.RIGHT_CLICK && hand == InteractionHand.OFF_HAND) {
      InteractionResult result = super.onToolUse(tool, modifier, player, hand, source);
      // also allow just blocking when used in main hand
      if (result.consumesAction()) {
        GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
        return InteractionResult.CONSUME;
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  public void onFinishUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
    if (entity instanceof Player player) {
      player.getCooldowns().addCooldown(new ItemStack(tool.getItem()), (int)(20 / tool.getStats().get(ToolStats.ATTACK_SPEED)));
    }
  }

  @Override
  public ItemUseAnimation getUseAction(IToolStackView tool, ModifierEntry modifier) {
    return ItemUseAnimation.BLOCK;
  }

  @Override
  public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
    return 20;
  }

  @Override
  public boolean canPerformAction(IToolStackView tool, ModifierEntry modifier, ItemAbility toolAction) {
    return toolAction == TinkerToolActions.SHIELD_BLOCK;
  }
}
