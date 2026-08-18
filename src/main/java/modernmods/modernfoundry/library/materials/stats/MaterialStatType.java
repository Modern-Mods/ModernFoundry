package modernmods.modernfoundry.library.materials.stats;

import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.mantle.registration.object.IdAwareObject;

import java.util.function.Function;

/**
 * <p>Part types are actually different material stat types.
 * Think of them as a collection of attributes a material has, when it's used for a specific part.
 * e.g. for a material to be used as a bowstring, it needs to have bowstring material stats.</p>
 *
 * <p>Each instance of this class should be unique. If two instances with the same id exist, internal systems might break.</p>
 */
public class MaterialStatType<T extends IMaterialStats> implements IdAwareObject {
  /** Context key to use if you want the recipe serializer passed into your recipe */
  public static final ContextKey<MaterialStatType<?>> CONTEXT_KEY = new ContextKey<>("material_stat_type");

  private final MaterialStatsId id;
  private final T defaultStats;
  private final RecordLoadable<T> loadable;
  private final boolean canRepair;

  /** Gets the raw identifier of this stat type, for {@link IdAwareObject} */
  @Override
  public Identifier getId() {
    return id.getIdentifier();
  }

  /** Gets the typed stat type ID */
  public MaterialStatsId getStatId() {
    return id;
  }

  /** Default stats instance for this type */
  public T getDefaultStats() {
    return defaultStats;
  }

  /** Loadable for parsing this stat type */
  public RecordLoadable<T> getLoadable() {
    return loadable;
  }

  /** If true, materials with this stat type can repair tools */
  public boolean canRepair() {
    return canRepair;
  }

  /** Creates a stat type using the given default instance */
  public MaterialStatType(MaterialStatsId id, T defaultStats, RecordLoadable<T> loadable) {
    this.id = id;
    this.defaultStats = defaultStats;
    this.loadable = loadable;
    this.canRepair = defaultStats instanceof IRepairableMaterialStats;
  }

  /**
   * Creates a stat type that wishes to store the stat type in a field. Use {@link MaterialStatType#CONTEXT_KEY} to fetch the type in the loadable.
   */
  public MaterialStatType(MaterialStatsId id, Function<MaterialStatType<T>,T> defaultStatsProvider, RecordLoadable<T> loadable) {
    this.id = id;
    this.loadable = loadable;
    this.defaultStats = defaultStatsProvider.apply(this);
    this.canRepair = defaultStats instanceof IRepairableMaterialStats;
  }

  /** Creates a stat type that always resolves to the same instance */
  public static <T extends IMaterialStats> MaterialStatType<T> singleton(MaterialStatsId id, T instance) {
    return new MaterialStatType<>(id, instance, new SingletonLoader<>(instance));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MaterialStatType<?> that = (MaterialStatType<?>) o;
    return this.id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }
}
