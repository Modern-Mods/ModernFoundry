package modernmods.modernfoundry.tools.modifiers.ability.armor;

import modernmods.modernfoundry.library.modifiers.modules.behavior.ShowOffhandModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modifiers.ability.tool.OffhandAttackModifier;

public class AmbidextrousModifier extends OffhandAttackModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ShowOffhandModule.DISALLOW_BROKEN);
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return true;
  }
}
