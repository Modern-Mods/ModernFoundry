package modernmods.modernfoundry.tools.modules.combat;
import modernmods.modernfoundry.tools.TinkerToolActions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolActionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module implementing {@link modernmods.modernfoundry.tools.data.ModifierIds#blocking} */
public enum BlockingModule implements ModifierModule, GeneralInteractionModifierHook, ToolActionModifierHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<BlockingModule>defaultHooks(ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOL_ACTION);
  public static final RecordLoadable<BlockingModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<BlockingModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Integer getPriority() {
    // late as many modifiers have special blocking interactions
    return 50;
  }

  @Override
  public boolean canPerformAction(IToolStackView tool, ModifierEntry entry, ItemAbility toolAction) {
    return toolAction == TinkerToolActions.SHIELD_BLOCK;
  }

  @Override
  public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
    if (source == InteractionSource.RIGHT_CLICK && !tool.isBroken()) {
      GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
      return InteractionResult.CONSUME;
    }
    return InteractionResult.PASS;
  }

  @Override
  public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
    return 72000;
  }

  @Override
  public ItemUseAnimation getUseAction(IToolStackView tool, ModifierEntry modifier) {
    return ItemUseAnimation.BLOCK;
  }
}
