package modernmods.modernfoundry.library.tools.definition.module.display;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/** Tool name that always shows the same material */
public record FixedMaterialToolName(int index) implements ToolNameHook.FromDefault, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FixedMaterialToolName>defaultHooks(ToolHooks.DISPLAY_NAME);
  /** Instance for an index of 0 */
  public static final FixedMaterialToolName FIRST = new FixedMaterialToolName(0);
  /** Loader instance */
  public static final RecordLoadable<FixedMaterialToolName> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ZERO.requiredField("index", FixedMaterialToolName::index),
    index -> {
      // most tools want first, so use a singleton
      if (index == 0) {
        return FIRST;
      }
      return new FixedMaterialToolName(index);
    }
  );

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FixedMaterialToolName> getLoader() {
    return LOADER;
  }

  @Override
  public Component getDisplayName(ToolDefinition definition, ItemStack stack, @Nullable IToolStackView tool, Component itemName) {
    MaterialVariantId material = ToolNameHook.getTool(stack, tool).getMaterials().get(index).getVariant();
    if (IMaterial.UNKNOWN_ID.equals(material)) {
      return itemName;
    }
    return MaterialToolName.nameForMaterial(material, itemName);
  }
}
