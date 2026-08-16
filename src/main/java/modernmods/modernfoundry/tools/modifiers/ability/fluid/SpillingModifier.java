package modernmods.modernfoundry.tools.modifiers.ability.fluid;

import net.neoforged.neoforge.fluids.FluidType;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.build.StatBoostModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.tools.modules.combat.SpillingModule;

/** @deprecated use {@link SpillingModule}, {@link ToolTankHelper#CAPACITY_STAT}, and {@link ToolTankHelper#TANK_HANDLER} */
@Deprecated(forRemoval = true)
public class SpillingModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ToolTankHelper.TANK_HANDLER);
    hookBuilder.addModule(StatBoostModule.add(ToolTankHelper.CAPACITY_STAT).eachLevel(FluidType.BUCKET_VOLUME));
    hookBuilder.addModule(new SpillingModule(LevelingValue.eachLevel(1), ModifierCondition.ANY_TOOL));
  }
}
