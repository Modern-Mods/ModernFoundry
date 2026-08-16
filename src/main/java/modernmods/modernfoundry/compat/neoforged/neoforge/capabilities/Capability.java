package modernmods.modernfoundry.compat.neoforged.neoforge.capabilities;

import modernmods.modernfoundry.compat.neoforged.neoforge.common.util.LazyOptional;

/**
 * Standalone functional replacement for the old Forge {@code Capability} token, used by Tinkers' capability
 * utility code. NeoForge replaced the capability system entirely; this lightweight token keeps the ported
 * provider code compiling and behaving consistently. Each distinct capability is represented by its own
 * instance, matched by identity.
 */
public class Capability<T> {
  public Capability() {}

  /**
   * Returns the given instance cast to this capability's type if {@code toCheck} is this capability,
   * otherwise an empty optional. Mirrors the old Forge {@code Capability.orEmpty} helper.
   */
  public <R> LazyOptional<R> orEmpty(Capability<R> toCheck, LazyOptional<?> instance) {
    return toCheck == this ? instance.cast() : LazyOptional.empty();
  }
}
