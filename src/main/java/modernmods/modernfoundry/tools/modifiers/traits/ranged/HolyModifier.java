package modernmods.modernfoundry.tools.modifiers.traits.ranged;

import modernmods.modernfoundry.compat.minecraft.world.entity.MobType;
import modernmods.hilt.data.predicate.entity.MobTypePredicate;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.combat.ConditionalPowerModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;

/** @deprecated use {@link ConditionalPowerModule} */
@Deprecated(forRemoval = true)
public class HolyModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ConditionalPowerModule.builder().target(new MobTypePredicate(MobType.UNDEAD)).eachLevel(0.75f));
  }
}
