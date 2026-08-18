package modernmods.modernfoundry.common.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/** @deprecated use {@link modernmods.mantle.registration.deferred.EnumDeferredRegister} */
@Deprecated(forRemoval = true)
public class EnumDeferredRegister<T> extends modernmods.mantle.registration.deferred.EnumDeferredRegister<T> {
  public EnumDeferredRegister(ResourceKey<Registry<T>> reg, String modID) {
    super(reg, modID);
  }
}
