package modernmods.modernfoundry.tools.modifiers.ability.tool;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidType;
import modernmods.hilt.data.predicate.fluid.FluidPredicate;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.modules.behavior.ShowOffhandModule;
import modernmods.modernfoundry.library.modifiers.modules.build.StatBoostModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.interaction.BucketModule;
import modernmods.modernfoundry.tools.modules.interaction.TankInteractionModule;

import javax.annotation.Nullable;

/** @deprecated use {@link BucketModule}, {@link ToolTankHelper#TANK_HANDLER}, {@link TankInteractionModule}, and {@link ShowOffhandModule} */
@Deprecated(forRemoval = true)
public class BucketingModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ToolTankHelper.TANK_HANDLER);
    hookBuilder.addModule(StatBoostModule.add(ToolTankHelper.CAPACITY_STAT).flat(FluidType.BUCKET_VOLUME));
    hookBuilder.addModule(new TankInteractionModule(InteractionSource.ARMOR));
    hookBuilder.addModule(new BucketModule(FluidPredicate.ANY));
    hookBuilder.addModule(ShowOffhandModule.ALLOW_BROKEN);
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, @Nullable RegistryAccess access) {
    return InteractionSource.formatModifierName(tool, this, super.getDisplayName(tool, entry, access));
  }
}
