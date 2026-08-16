package modernmods.modernfoundry.smeltery.block.entity;

import net.minecraft.core.Direction;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.common.util.LazyOptional;

import javax.annotation.Nullable;

/**
 * Bridge interface exposing the legacy Forge {@code getCapability(Capability, Direction)} accessor so shared
 * capability registration helpers can query block entities generically.
 * TODO(neoforge-capabilities): replace legacy capability wiring with RegisterCapabilitiesEvent providers.
 */
public interface ILegacyCapabilityBlockEntity {
  <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing);
}
