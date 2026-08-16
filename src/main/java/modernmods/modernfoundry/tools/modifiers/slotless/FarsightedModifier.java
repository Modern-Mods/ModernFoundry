package modernmods.modernfoundry.tools.modifiers.slotless;

import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.FovModule;
import modernmods.modernfoundry.tools.modules.FovModule.FovAction;

/** @deprecated use {@link FovModule} */
@Deprecated(forRemoval = true)
public class FarsightedModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new FovModule(LevelingValue.eachLevel(0.05f), FovAction.DECREASE));
  }
}
