package modernmods.modernfoundry.tools.modifiers.upgrades.melee;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.FieryCounterModule;
import modernmods.modernfoundry.tools.modules.combat.FieryAttackModule;

/** @deprecated use {@link FieryAttackModule} and {@link FieryCounterModule} */
@Deprecated(forRemoval = true)
public class FieryModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new FieryAttackModule(LevelingValue.eachLevel(5)));
    hookBuilder.addModule(FieryCounterModule.builder().constant(LevelingValue.eachLevel(5)).toolTag(TinkerTags.Items.ARMOR).build());
  }
}
