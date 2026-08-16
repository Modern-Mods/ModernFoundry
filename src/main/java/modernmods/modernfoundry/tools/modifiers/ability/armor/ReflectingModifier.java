package modernmods.modernfoundry.tools.modifiers.ability.armor;

import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.build.VolatileIntModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.logic.ModifierEvents;

/** @deprecated use {@link VolatileIntModule} with {@link ModifierEvents#REFLECTING} */
@Deprecated(forRemoval = true)
public class ReflectingModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new VolatileIntModule(ModifierEvents.REFLECTING, LevelingInt.eachLevel(40)));
  }
}
