package modernmods.modernfoundry.tools.modifiers.ability.fluid;

import net.neoforged.neoforge.fluids.FluidType;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.build.StatBoostModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.tools.modules.interaction.SlurpingModule;

/** @deprecated use {@link SlurpingModule} */
@Deprecated(forRemoval = true)
public class SlurpingModifier extends Modifier {
  @Override
  public int getPriority() {
    return 40;
  }

  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new SlurpingModule(LevelingValue.eachLevel(1), LevelingInt.flat(21)));
    hookBuilder.addModule(ToolTankHelper.TANK_HANDLER);
    hookBuilder.addModule(StatBoostModule.add(ToolTankHelper.CAPACITY_STAT).eachLevel(FluidType.BUCKET_VOLUME));
  }
}
