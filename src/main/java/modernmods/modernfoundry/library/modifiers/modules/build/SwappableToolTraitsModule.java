package modernmods.modernfoundry.library.modifiers.modules.build;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.primitive.StringLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierTraitHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.util.ModuleWithKey;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.SlotType;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.build.ToolTraitHook;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

import static modernmods.modernfoundry.library.modifiers.modules.build.SwappableSlotModule.FORMAT;

/** Module to add additional traits to a tool given the passed hook */
public class SwappableToolTraitsModule implements ModifierModule, ModifierTraitHook, DisplayNameModifierHook, ModifierRemovalHook, ModuleWithKey {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SwappableToolTraitsModule>defaultHooks(ModifierHooks.MODIFIER_TRAITS, ModifierHooks.DISPLAY_NAME, ModifierHooks.REMOVE);
  @SuppressWarnings("unchecked")
  public static final RecordLoadable<SwappableToolTraitsModule> LOADER = RecordLoadable.create(
    ModuleWithKey.FIELD,
    StringLoadable.DEFAULT.defaultField("match", "", false, m -> m.match),
    ToolHooks.LOADER.comapFlatMap((hook, error) -> {
      if (!hook.supportsHook(ToolTraitHook.class)) {
        throw error.create("Hook " + hook.getId() + " is not valid for ToolTraitHook");
      }
      return (ModuleHook<ToolTraitHook>) hook;
    }, hook -> hook).requiredField("hook", m -> m.hook),
    SwappableToolTraitsModule::new);

  @Nullable
  @Getter @Accessors(fluent = true)
  private final Identifier key;
  private final String match;
  private final ModuleHook<ToolTraitHook> hook;
  private final Component component;

  public SwappableToolTraitsModule(@Nullable Identifier key, String match, ModuleHook<ToolTraitHook> hook) {
    this.key = key;
    this.match = match;
    this.hook = hook;
    // TODO: this shouldn't hardcode to modifier slot types
    this.component = Component.translatable(SlotType.KEY_DISPLAY + match);
  }

  @Override
  public RecordLoadable<? extends SwappableToolTraitsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    if (!match.isEmpty() && match.equals(tool.getPersistentData().getString(getKey(entry.getModifier())))) {
      return Component.translatable(FORMAT, name.plainCopy(), component).withStyle(name.getStyle());
    }
    return name;
  }


  @Override
  public void addTraits(IToolContext context, ModifierEntry modifier, TraitBuilder builder, boolean firstEncounter) {
    // TODO: switch to using module conditions, move the match to a custom predicate
    if (match.isEmpty() || match.equals(context.getPersistentData().getString(getKey(modifier.getModifier())))) {
      ToolDefinition definition = context.getDefinition();
      definition.getHook(hook).addTraits(definition, context.getMaterials(), builder);
    }
  }

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    if (!match.isEmpty()) {
      tool.getPersistentData().remove(getKey(modifier));
    }
    return null;
  }
}
