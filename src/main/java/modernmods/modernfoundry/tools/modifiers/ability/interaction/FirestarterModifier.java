package modernmods.modernfoundry.tools.modifiers.ability.interaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.modifiers.modules.behavior.ShowOffhandModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.interaction.PlaceFireModule;

import javax.annotation.Nullable;

/** @deprecated use {@link PlaceFireModule} and {@link ShowOffhandModule} */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor
public class FirestarterModifier extends NoLevelsModifier {
  @Getter
  private final int priority;

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ShowOffhandModule.DISALLOW_BROKEN);
    hookBuilder.addModule(PlaceFireModule.INSTANCE);
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return priority > Short.MIN_VALUE;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, @Nullable RegistryAccess access) {
    return InteractionSource.formatModifierName(tool, this, super.getDisplayName(tool, entry, access));
  }
}
