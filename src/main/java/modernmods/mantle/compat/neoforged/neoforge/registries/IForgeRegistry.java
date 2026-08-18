package modernmods.mantle.compat.neoforged.neoforge.registries;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Thin adapter mirroring the old Forge {@code IForgeRegistry} surface on top of a NeoForge
 * {@link net.minecraft.core.Registry}. Only the members used by the ported code are exposed.
 */
public interface IForgeRegistry<T> extends Iterable<T> {
  /** Checks if the registry contains the given id */
  boolean containsKey(Identifier id);

  /** Gets the value for the given id, or null if absent */
  T getValue(Identifier id);

  /** Registers a value; only valid while the backing registry is unfrozen */
  void register(Identifier id, T value);

  /** Gets all registered values */
  Collection<T> getValues();

  /** Gets all registered entries */
  Set<Map.Entry<ResourceKey<T>, T>> getEntries();
}
