package modernmods.modernfoundry.tools.modifiers.upgrades.armor;

import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.combat.KnockbackModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.KnockbackCounterModule;

/** @deprecated use {@link KnockbackModule} and {@link KnockbackCounterModule} */
@Deprecated(forRemoval = true)
public class SpringyModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(KnockbackModule.builder().eachLevel(0.5f));
    hookBuilder.addModule(KnockbackCounterModule.builder().durabilityUsage(0).random(LevelingValue.eachLevel(0.5f)).chanceLeveling(0.25f).build());
  }
}
