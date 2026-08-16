package modernmods.modernfoundry.library.modifiers.modules.capacity;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import modernmods.modernfoundry.library.modifiers.IncrementalModifierEntry;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ValidateModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.CapacityBarHook;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/** Implements cleanup and removal of capacity bars, used mainly for durability bars */
public record CapacityBarValidator(CapacityBarHook bar) implements HookProvider, DisplayNameModifierHook, ValidateModifierHook, ModifierRemovalHook {
  private static final List<ModuleHook<?>> HOOKS = HookProvider.<CapacityBarValidator>defaultHooks(ModifierHooks.DISPLAY_NAME, ModifierHooks.VALIDATE, ModifierHooks.REMOVE);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return HOOKS;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    return IncrementalModifierEntry.addAmountToName(entry.getModifier().getDisplayName(entry.getLevel()), bar.getAmount(tool), bar.getCapacity(tool, entry));
  }

  @Nullable
  @Override
  public Component validate(IToolStackView tool, ModifierEntry modifier) {
    // clear excess amount
    int cap = bar.getCapacity(tool, modifier);
    if (bar.getCapacity(tool, modifier) > cap) {
      bar.setAmount(tool, modifier, cap);
    }
    return null;
  }

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    bar.setAmount(tool, ModifierEntry.EMPTY, 0);
    return null;
  }
}
