package modernmods.modernfoundry.library.tools.definition.module.interaction;

import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.json.predicate.modifier.ModifierPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/**
 * Interaction that makes only a limited set work in the preferred hand, the rest working in the other hand.
 * TODO 1.21: rework fields to be more like {@link ToggleableSetInteraction}
 */
public record PreferenceSetInteraction(InteractionSource preferredSource, IJsonPredicate<ModifierId> preferenceModifiers) implements InteractionToolModule, ToolModule {
  public static final RecordLoadable<PreferenceSetInteraction> LOADER = RecordLoadable.create(
    TinkerLoadables.INTERACTION_SOURCE.requiredField("preferred_source", PreferenceSetInteraction::preferredSource),
    ModifierPredicate.LOADER.requiredField("preferred_modifiers", PreferenceSetInteraction::preferenceModifiers),
    PreferenceSetInteraction::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<PreferenceSetInteraction>defaultHooks(ToolHooks.INTERACTION);

  @Override
  public boolean canInteract(IToolStackView tool, ModifierId modifier, InteractionSource source) {
    return (source == preferredSource) == preferenceModifiers.matches(modifier);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<PreferenceSetInteraction> getLoader() {
    return LOADER;
  }
}
