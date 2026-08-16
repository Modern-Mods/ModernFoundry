package modernmods.modernfoundry.tools.modifiers.upgrades.ranged;

import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.ranged.common.ArrowPierceModule;

/** @deprecated use {@link ArrowPierceModule} */
@Deprecated(forRemoval = true)
public class ImpalingModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new ArrowPierceModule(LevelingInt.eachLevel(1), ModifierCondition.ANY_TOOL));
  }
}
