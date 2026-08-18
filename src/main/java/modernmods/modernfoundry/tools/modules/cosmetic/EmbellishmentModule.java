package modernmods.modernfoundry.tools.modules.cosmetic;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.client.materials.MaterialTooltipCache;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.build.RawDataModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.utils.RestrictedCompoundTag;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing {@link modernmods.modernfoundry.tools.TinkerModifiers#embellishment}. */
public enum EmbellishmentModule implements ModifierModule, DisplayNameModifierHook, ModifierRemovalHook, RawDataModifierHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<EmbellishmentModule>defaultHooks(ModifierHooks.DISPLAY_NAME, ModifierHooks.REMOVE, ModifierHooks.RAW_DATA);
  public static final RecordLoadable<EmbellishmentModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<? extends ModifierModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    MaterialVariantId materialVariant = MaterialVariantId.tryParse(tool.getPersistentData().getString(entry.getId().getIdentifier()));
    if (materialVariant != null) {
      return Component.translatable(entry.getModifier().getTranslationKey() + ".formatted", MaterialTooltipCache.getDisplayName(materialVariant)).withStyle(style -> style.withColor(MaterialTooltipCache.getColor(materialVariant)));
    }
    return name;
  }

  @Override
  public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag tag) {
    // on build, migrate material redirects
    ModDataNBT data = tool.getPersistentData();
    Identifier key = modifier.getId().getIdentifier();
    MaterialVariantId materialVariant = MaterialVariantId.tryParse(data.getString(key));
    if (materialVariant != null) {
      MaterialId original = materialVariant.getId();
      MaterialId resolved = MaterialRegistry.getInstance().resolve(original);
      // instance check is safe here as resolve returns same instance if no redirect
      if (resolved != original) {
        data.putString(key, MaterialVariantId.create(resolved, materialVariant.getVariant()).toString());
      }
    }
  }

  @Override
  public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag tag) {}

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    tool.getPersistentData().remove(modifier.getId().getIdentifier());
    return null;
  }
}
