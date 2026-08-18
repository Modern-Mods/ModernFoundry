package modernmods.modernfoundry.compat.neoforged.neoforge.capabilities;

import net.minecraft.core.Direction;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;

/** Compatibility shim for old serializable capability providers. */
public interface ICapabilitySerializable<T> {
  <C> LazyOptional<C> getCapability(Capability<C> cap, Direction side);

  T serializeNBT();

  void deserializeNBT(T nbt);
}
