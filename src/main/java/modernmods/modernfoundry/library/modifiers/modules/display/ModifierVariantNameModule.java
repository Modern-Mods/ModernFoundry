package modernmods.modernfoundry.library.modifiers.modules.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import modernmods.hilt.client.ResourceColorManager;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.build.SwappableSlotModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.recipe.modifiers.adding.SwappableModifierRecipe.VariantFormatter;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module for formatting the modifier variant name using a variant formatter
 * @see ModifierVariantColorModule
 */
public record ModifierVariantNameModule(VariantFormatter formatter) implements ModifierModule, DisplayNameModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierVariantNameModule>defaultHooks(ModifierHooks.DISPLAY_NAME);
  public static final RecordLoadable<ModifierVariantNameModule> LOADER = RecordLoadable.create(
    VariantFormatter.LOADER.requiredField("formatter", ModifierVariantNameModule::formatter),
    ModifierVariantNameModule::new);

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    String variant = tool.getPersistentData().getString(entry.getId());
    if (!variant.isEmpty()) {
      // allow overriding the color of the result using the resource color manager
      TextColor color = ResourceColorManager.getOrNull(entry.getModifier().getTranslationKey() + '.' + variant);
      Style style = name.getStyle();
      if (color != null) {
        style = style.withColor(color);
      }
      return Component.translatable(SwappableSlotModule.FORMAT, name.copy().withStyle(Style.EMPTY), formatter.format(entry.getId(), variant)).withStyle(style);
    }
    return name;
  }

  @Override
  public RecordLoadable<ModifierVariantNameModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
