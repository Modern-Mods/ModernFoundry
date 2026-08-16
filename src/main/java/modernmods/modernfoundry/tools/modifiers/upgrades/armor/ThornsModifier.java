package modernmods.modernfoundry.tools.modifiers.upgrades.armor;

import net.minecraft.world.damagesource.DamageTypes;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.ThornsModule;

/** @deprecated use {@link ThornsModule} */
@Deprecated(forRemoval = true)
public class ThornsModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ThornsModule.type(DamageTypes.THORNS).constantFlat(1).randomFlat(3).build());
  }
}
