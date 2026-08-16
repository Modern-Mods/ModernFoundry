package modernmods.modernfoundry.tools.modifiers.upgrades.melee;

import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.combat.SeveringModule;

/** @deprecated use {@link SeveringModule} */
@Deprecated(forRemoval = true)
public class SeveringModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(SeveringModule.INSTANCE);
  }
}
