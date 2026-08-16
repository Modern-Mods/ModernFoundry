package modernmods.modernfoundry.tools.modifiers.ability.armor;

import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.FireWalkerModule;

/** @deprecated use {@link FireWalkerModule} */
@Deprecated(forRemoval = true)
public class FlamewakeModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new FireWalkerModule(new LevelingValue(1.5f, 1)));
  }
}
