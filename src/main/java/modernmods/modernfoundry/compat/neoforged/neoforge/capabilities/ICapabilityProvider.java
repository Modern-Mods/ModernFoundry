package modernmods.modernfoundry.compat.neoforged.neoforge.capabilities;

import net.minecraft.core.Direction;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;

/** Compatibility shim for old Forge capability providers. */
public interface ICapabilityProvider {
  <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side);
}
