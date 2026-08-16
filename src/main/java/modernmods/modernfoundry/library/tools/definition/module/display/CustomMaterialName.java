package modernmods.modernfoundry.library.tools.definition.module.display;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.primitive.StringLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.client.materials.MaterialTooltipCache;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.helper.TooltipUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Translates a custom format key for the given material
 * @param index   Material index to fetch
 * @param suffix  Translation key suffix to apply to the material name.
 */
public record CustomMaterialName(int index, String suffix) implements ToolNameHook.FromDefault, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<CustomMaterialName>defaultHooks(ToolHooks.DISPLAY_NAME);
  /** Loader instance */
  public static final RecordLoadable<CustomMaterialName> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ZERO.requiredField("index", CustomMaterialName::index),
    StringLoadable.DEFAULT.requiredField("suffix", CustomMaterialName::suffix),
    CustomMaterialName::new);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<CustomMaterialName> getLoader() {
    return LOADER;
  }

  @Override
  public Component getDisplayName(ToolDefinition definition, ItemStack stack, @Nullable IToolStackView tool, Component itemName) {
    MaterialVariantId material = ToolNameHook.getTool(stack, tool).getMaterials().get(index).getVariant();
    if (IMaterial.UNKNOWN_ID.equals(material)) {
      return itemName;
    }
    // translate the suffixed key
    Component component;
    find: {
      // first, try the material directly
      String materialKey = MaterialTooltipCache.getKey(material) + '.' + suffix;
      if (Util.canTranslate(materialKey)) {
        component = Component.translatable(materialKey);
        break find;
      }
      // if that did not work, do base material
      if (material.hasVariant()) {
        materialKey = MaterialTooltipCache.getKey(material.getId()) + '.' + suffix;
        if (Util.canTranslate(materialKey)) {
          component = Component.translatable(materialKey);
          break find;
        }
      }
      // if both failed, use the regular key
      component = MaterialTooltipCache.getDisplayName(material);
    }
    return Component.translatable(TooltipUtil.KEY_FORMAT, component, itemName);
  }
}
