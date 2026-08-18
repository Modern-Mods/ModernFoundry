package modernmods.modernfoundry.library.tools.capability.fluid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.Mantle;
import modernmods.mantle.data.registry.NamedComponentRegistry;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.build.ModifierTraitModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.CapacityStat;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStatId;
import modernmods.modernfoundry.tools.TinkerModifiers;

import java.util.function.BiFunction;

/** Helper methods for handling fluids in tools */
@SuppressWarnings("ClassCanBeRecord")  // want to leave extendable
@Getter
@RequiredArgsConstructor
public class ToolTankHelper {
  /** Helper function to parse a fluid from NBT */
  public static final RegistryAccess.Frozen STATIC_REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
  public static final BiFunction<CompoundTag, String, FluidStack> PARSE_FLUID = (nbt, key) -> modernmods.modernfoundry.library.utils.TagUtil.readFluid(nbt.getCompoundOrEmpty(key));

  /** Format key for the stat */
  public static final String MB_FORMAT = Mantle.makeDescriptionId("gui", "fluid.millibucket");
  /** Stat controlling the max for the default helper */
  public static final CapacityStat CAPACITY_STAT = new CapacityStat(new ToolStatId(TConstruct.MOD_ID, "tank_capacity"), 0xA0A0A0, MB_FORMAT);
  /** Default tank helper for setting fluids */
  public static final ToolTankHelper TANK_HELPER = new ToolTankHelper(CAPACITY_STAT, TConstruct.getResource("tank_fluid"));
  /** Module ensuring the tool has the tank */
  public static final ModifierModule TANK_HANDLER = new ModifierTraitModule(TinkerModifiers.tankHandler.getId(), 1, true);

  /** Loadable instance for JSON */
  public static final NamedComponentRegistry<ToolTankHelper> LOADABLE = new NamedComponentRegistry<>("Unknown Tool Tank Helper");

  /** Tool stat handling max tank capacity */
  private final INumericToolStat<?> capacityStat;
  /** Key in persistent data storing the fluid */
  private final Identifier fluidKey;

  /** Gets the capacity for the tool */
  public int getCapacity(IToolStackView tool) {
    return tool.getStats().getInt(capacityStat);
  }


  /* Fluid */

  /** Gets the fluid in the tank */
  public FluidStack getFluid(IToolStackView tool) {
    return tool.getPersistentData().get(getFluidKey(), PARSE_FLUID);
  }

  /** Sets the fluid in the tank */
  public FluidStack setFluid(IToolStackView tool, FluidStack fluid) {
    if (fluid.isEmpty()) {
      tool.getPersistentData().remove(fluidKey);
      return FluidStack.EMPTY;
    }
    int capacity = getCapacity(tool);
    // we always copy before saving to ensure the NBT on the fluid gets copied, since those being the same compound is possible
    fluid = fluid.copy();
    if (fluid.getAmount() > capacity) {
      fluid.setAmount(capacity);
    }
    Tag tag = modernmods.modernfoundry.library.utils.TagUtil.writeFluid(fluid);
    if (tag instanceof CompoundTag compound) {
      tool.getPersistentData().put(fluidKey, compound);
    }
    return fluid;
  }
}
