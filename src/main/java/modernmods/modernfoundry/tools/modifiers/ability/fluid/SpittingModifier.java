package modernmods.modernfoundry.tools.modifiers.ability.fluid;

import net.neoforged.neoforge.fluids.FluidType;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.build.StatBoostModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.tools.modules.interaction.SpittingModule;

/** @deprecated use {@link SpittingModule} */
@Deprecated(forRemoval = true)
public class SpittingModifier extends Modifier {
  @Override
  protected void registerHooks(Builder builder) {
    builder.addModule(new SpittingModule(LevelingInt.eachLevel(1)));
    builder.addModule(ToolTankHelper.TANK_HANDLER);
    builder.addModule(StatBoostModule.add(ToolTankHelper.CAPACITY_STAT).eachLevel(FluidType.BUCKET_VOLUME));
  }

  @Override
  public int getPriority() {
    return 120; // want to run before sling modifiers so we can sling spit, and before throwing so we use our tank first
  }
}
