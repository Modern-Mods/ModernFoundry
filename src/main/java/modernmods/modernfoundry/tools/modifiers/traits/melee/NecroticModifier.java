package modernmods.modernfoundry.tools.modifiers.traits.melee;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.RestoreLostHealthModule;
import modernmods.modernfoundry.tools.modules.combat.LifestealModule;

/** @deprecated use {@link LifestealModule} and {@link RestoreLostHealthModule} */
@Deprecated(forRemoval = true)
public class NecroticModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(LifestealModule.builder().eachLevel(0.05f));
    hookBuilder.addModule(RestoreLostHealthModule.builder().toolTag(TinkerTags.Items.ARMOR).eachLevel(0.25f));
  }
}
