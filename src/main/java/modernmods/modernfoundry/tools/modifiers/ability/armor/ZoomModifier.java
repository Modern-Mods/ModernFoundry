package modernmods.modernfoundry.tools.modifiers.ability.armor;

import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.ZoomModule;

/** @deprecated use {@link ZoomModule} */
@Deprecated(forRemoval = true)
public class ZoomModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ZoomModule.SPYGLASS);
  }

  @Override
  public int getPriority() {
    return 90; // after slurping and slings, before blocking
  }
}
