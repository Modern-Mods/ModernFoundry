package modernmods.modernfoundry.compat.neoforged.neoforge.event;

import net.minecraft.resources.Identifier;

/** Compatibility shim for removed attach capability event. */
public class AttachCapabilitiesEvent<T> {
  private final T object;

  public AttachCapabilitiesEvent(T object) {
    this.object = object;
  }

  public T getObject() {
    return object;
  }

  public void addCapability(Identifier id, Object provider) {}

  public void addListener(Runnable listener) {}
}
