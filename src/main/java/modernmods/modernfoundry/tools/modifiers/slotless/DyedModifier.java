package modernmods.modernfoundry.tools.modifiers.slotless;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.cosmetic.DyeModule;

import javax.annotation.Nullable;

/** @deprecated use {@link DyeModule} */
@Deprecated(forRemoval = true)
public class DyedModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(DyeModule.INSTANCE);
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, @Nullable RegistryAccess access) {
    return DyeModule.INSTANCE.getDisplayName(tool, entry, super.getDisplayName(), access);
  }
}
