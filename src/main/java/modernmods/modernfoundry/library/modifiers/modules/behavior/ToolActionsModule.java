package modernmods.modernfoundry.library.modifiers.modules.behavior;

import com.google.common.collect.ImmutableSet;
import net.neoforged.neoforge.common.ItemAbility;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolActionModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Set;

/**
 * Module that allows a modifier to perform tool actions
 */
public record ToolActionsModule(Set<ItemAbility> actions, ModifierCondition<IToolStackView> condition) implements ToolActionModifierHook, ModifierModule, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ToolActionsModule>defaultHooks(ModifierHooks.TOOL_ACTION);
  public static final RecordLoadable<ToolActionsModule> LOADER = RecordLoadable.create(
    Loadables.TOOL_ACTION.set().requiredField("tool_actions", ToolActionsModule::actions),
    ModifierCondition.TOOL_FIELD,
    ToolActionsModule::new);

  public ToolActionsModule(ItemAbility... actions) {
    this(ImmutableSet.copyOf(actions), ModifierCondition.ANY_TOOL);
  }

  @Override
  public boolean canPerformAction(IToolStackView tool, ModifierEntry modifier, ItemAbility toolAction) {
    return condition.matches(tool, modifier) && actions.contains(toolAction);
  }

  @Override
  public RecordLoadable<ToolActionsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
