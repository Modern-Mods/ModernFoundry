package modernmods.mantle.compat.neoforged.neoforge.registries;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Concrete {@link IForgeRegistry} backed by a NeoForge {@link Registry}. Adds the old Forge
 * {@code unfreeze()} escape hatch used by datagen fake-entry helpers.
 */
public class ForgeRegistry<T> implements IForgeRegistry<T> {
  private final Registry<T> registry;

  public ForgeRegistry(Registry<T> registry) {
    this.registry = registry;
  }

  /** Gets the backing NeoForge registry */
  public Registry<T> unwrap() {
    return registry;
  }

  @Override
  public boolean containsKey(Identifier id) {
    return registry.containsKey(id);
  }

  @Override
  public T getValue(Identifier id) {
    return registry.getValue(id);
  }

  @Override
  public void register(Identifier id, T value) {
    Registry.register(registry, id, value);
  }

  @Override
  public Collection<T> getValues() {
    List<T> values = new ArrayList<>();
    registry.forEach(values::add);
    return values;
  }

  @Override
  public Set<Map.Entry<ResourceKey<T>, T>> getEntries() {
    return registry.entrySet();
  }

  @Override
  public Iterator<T> iterator() {
    return registry.iterator();
  }

  /** Unfreezes the backing registry so datagen can inject fake entries (datagen only) */
  public void unfreeze() {
    if (registry instanceof MappedRegistry<T> mapped) {
      try {
        Method method = MappedRegistry.class.getDeclaredMethod("unfreeze");
        method.setAccessible(true);
        method.invoke(mapped);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Unable to unfreeze registry", e);
      }
    }
  }
}
