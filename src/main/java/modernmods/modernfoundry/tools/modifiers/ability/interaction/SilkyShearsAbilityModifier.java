package modernmods.modernfoundry.tools.modifiers.ability.interaction;

import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modules.interaction.ShearsModule;

/** @deprecated use {@link ShearsModule} with a condition of silky */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public class SilkyShearsAbilityModifier extends ShearsAbilityModifier {
  public SilkyShearsAbilityModifier(int range, int priority) {
    super(range, priority);
  }
  
  @Override
  protected boolean isShears(IToolStackView tool) {
    return tool.getModifierLevel(TinkerModifiers.silky.getId()) > 0;
  }
}
