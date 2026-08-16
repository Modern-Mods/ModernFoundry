package modernmods.modernfoundry.compat.neoforged.neoforge.capabilities;

import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.Capability;

/** Compatibility shim for old Forge capability declarations. */
public final class CapabilityManager {
  private CapabilityManager() {}

  public static <T> Capability<T> get(CapabilityToken<T> token) {
    return new Capability<>();
  }
}
