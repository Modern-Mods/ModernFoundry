package modernmods.modernfoundry.tools.modifiers.slotless;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.cosmetic.TrimModule;

import javax.annotation.Nullable;

/** @deprecated use {@link modernmods.modernfoundry.tools.modules.cosmetic.TrimModule} */
@Deprecated(forRemoval = true)
public class TrimModifier extends NoLevelsModifier {
  private static final TrimModule TRIM = new TrimModule();
  /** @deprecated use {@link TrimModule#patternKey(ModifierId)} */
  @Deprecated(forRemoval = true)
  public static final Identifier TRIM_PATTERN = TConstruct.getResource("trim_pattern");
  /** @deprecated use {@link TrimModule#materialKey(ModifierId)} */
  @Deprecated(forRemoval = true)
  public static final Identifier TRIM_MATERIAL = TConstruct.getResource("trim_material");

  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(TRIM);
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, @Nullable RegistryAccess access) {
    return TRIM.getDisplayName(tool, entry, getDisplayName(), access);
  }
}
