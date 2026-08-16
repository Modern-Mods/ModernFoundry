package modernmods.modernfoundry.tools.modifiers.upgrades.armor;

import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.SoulSpeedModule;

/** @deprecated use {@link SoulSpeedModule} */
@Deprecated(forRemoval = true)
public class SoulSpeedModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new SoulSpeedModule(LevelingInt.flat(1), ModifierCondition.ANY_TOOL));
  }
}
