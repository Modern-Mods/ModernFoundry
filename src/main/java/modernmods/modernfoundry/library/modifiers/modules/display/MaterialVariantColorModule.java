package modernmods.modernfoundry.library.modifiers.modules.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.client.materials.MaterialTooltipCache;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.definition.MaterialVariant;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that colors the display name based on the variant.
 * @see ModifierVariantColorModule
 */
public record MaterialVariantColorModule(MaterialId material) implements ModifierModule, DisplayNameModifierHook {
  public static final RecordLoadable<MaterialVariantColorModule> LOADER = RecordLoadable.create(MaterialId.PARSER.requiredField("material", MaterialVariantColorModule::material), MaterialVariantColorModule::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaterialVariantColorModule>defaultHooks(ModifierHooks.DISPLAY_NAME);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<MaterialVariantColorModule> getLoader() {
    return LOADER;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    // use the color of the first match found
    for (MaterialVariant material : tool.getMaterials()) {
      if (this.material.equals(material.getId())) {
        return name.copy().withStyle(style -> style.withColor(MaterialTooltipCache.getColor(material.getVariant())));
      }
    }
    return name;
  }
}
