package modernmods.modernfoundry.library.tools.definition.module.display;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.client.materials.MaterialTooltipCache;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

/** Tool name that always shows the same material */
public record UniqueMaterialToolName(int index) implements ToolNameHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<UniqueMaterialToolName>defaultHooks(ToolHooks.DISPLAY_NAME);
  /** Instance for an index of 0 */
  public static final UniqueMaterialToolName FIRST = new UniqueMaterialToolName(0);
  /** Instance for an index of 1 */
  public static final UniqueMaterialToolName SECOND = new UniqueMaterialToolName(1);
  /** Loader instance */
  public static final RecordLoadable<UniqueMaterialToolName> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ZERO.requiredField("index", UniqueMaterialToolName::index),
    index -> {
      // common numbers
      if (index == 0) return FIRST;
      if (index == 1) return SECOND;
      return new UniqueMaterialToolName(index);
    }
  );

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<UniqueMaterialToolName> getLoader() {
    return LOADER;
  }

  @Override
  public Component getDisplayName(ToolDefinition definition, ItemStack stack, @Nullable IToolStackView tool) {
    String key = stack.getItem().getDescriptionId();
    MaterialVariantId material = ToolNameHook.getTool(stack, tool).getMaterials().get(index).getVariant();
    if (!IMaterial.UNKNOWN_ID.equals(material)) {
      String materialKey = key + '.' + MaterialTooltipCache.getKey(material);
      // if we can translate it, we can use it
      if (Util.canTranslate(materialKey)) {
        key = materialKey;
      // if we cannot, try the base key to use that
      } else if (material.hasVariant()) {
        materialKey = key + MaterialTooltipCache.getKey(material.getId());
        if (Util.canTranslate(materialKey)) {
          key = materialKey;
        }
      }
    }
    return Component.translatable(key);
  }
}
