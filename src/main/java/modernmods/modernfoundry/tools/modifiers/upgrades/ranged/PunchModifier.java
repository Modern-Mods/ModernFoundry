package modernmods.modernfoundry.tools.modifiers.upgrades.ranged;

import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.ranged.common.PunchModule;

/** @deprecated use {@link PunchModule} */
@Deprecated(forRemoval = true)
public class PunchModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new PunchModule(LevelingValue.eachLevel(1), ModifierCondition.ANY_TOOL));
  }
}
