package modernmods.modernfoundry.tools.modifiers.upgrades.ranged;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.FreezingCounterModule;
import modernmods.modernfoundry.tools.modules.combat.FreezingAttackModule;

/** @deprecated use {@link FreezingAttackModule} and {@link FreezingCounterModule} */
@Deprecated(forRemoval = true)
public class FreezingModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new FreezingAttackModule(new LevelingValue(4, 4)));
    hookBuilder.addModule(FreezingCounterModule.builder().constant(new LevelingValue(4, 4)).toolTag(TinkerTags.Items.ARMOR).build());
  }
}
