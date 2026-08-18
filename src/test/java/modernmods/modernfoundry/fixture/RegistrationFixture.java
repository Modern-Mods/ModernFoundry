package modernmods.modernfoundry.fixture;

import net.minecraft.resources.ResourceLocation;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.registry.GenericLoaderRegistry;

/** Helpers for generic registration tasks */
public class RegistrationFixture {
  /** Registers an object to a registry without risk of tests failing if its registered already */
  public static <T> void register(GenericLoaderRegistry<? super T> registry, String name, RecordLoadable<T> value) {
    try {
      registry.register(new ResourceLocation("test", name), value);
    } catch (Exception e) {
      // no-op
    }
  }
}
