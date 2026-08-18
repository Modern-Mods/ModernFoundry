package modernmods.modernfoundry.library.utils;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * Helper for use with our extensions of resource location for some type safety in IDs.
 * <p>
 * 26.1 made {@link Identifier} final, so this can no longer extend it. Instead it wraps an {@link Identifier} and
 * delegates the common accessors; use {@link #getIdentifier()} where a raw {@link Identifier} is required.
 * @see IdParser
 */
public abstract class ResourceId implements Comparable<ResourceId> {
  /** Wrapped identifier */
  private final Identifier id;

  public ResourceId(Identifier location) {
    this.id = location;
  }

  public ResourceId(String namespace, String path) {
    this.id = Identifier.fromNamespaceAndPath(namespace, path);
  }

  public ResourceId(String location) {
    this.id = Identifier.parse(location);
  }

  /** Gets the wrapped identifier, for use where a raw {@link Identifier} is required */
  public Identifier getIdentifier() {
    return id;
  }


  /* Delegated identifier accessors */

  public String getNamespace() {
    return id.getNamespace();
  }

  public String getPath() {
    return id.getPath();
  }

  public Identifier withPath(String path) {
    return id.withPath(path);
  }

  public Identifier withPath(UnaryOperator<String> path) {
    return id.withPath(path);
  }

  public Identifier withPrefix(String prefix) {
    return id.withPrefix(prefix);
  }

  public Identifier withSuffix(String suffix) {
    return id.withSuffix(suffix);
  }

  public String toLanguageKey() {
    return id.toLanguageKey();
  }

  public String toLanguageKey(String type) {
    return id.toLanguageKey(type);
  }

  public String toLanguageKey(String type, String key) {
    return id.toLanguageKey(type, key);
  }

  public String toShortString() {
    return id.toShortString();
  }

  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof ResourceId other) {
      return id.equals(other.id);
    }
    if (o instanceof Identifier other) {
      return id.equals(other);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public int compareTo(ResourceId o) {
    return id.compareTo(o.id);
  }


  /* Helpers for static constructors */

  /**
   * Creates a new ID from the given string
   * @param string  String
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryParse(String string, BiFunction<String,String,T> constructor) {
    Identifier location = Identifier.tryParse(string);
    if (location == null) {
      return null;
    }
    return tryBuild(location.getNamespace(), location.getPath(), constructor);
  }

  /**
   * Creates a new ID from the given namespace and path
   * @param namespace  Namespace
   * @param path       Path
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryBuild(String namespace, String path, BiFunction<String,String,T> constructor) {
    if (Identifier.isValidNamespace(namespace) && Identifier.isValidPath(path)) {
      return constructor.apply(namespace, path);
    }
    return null;
  }
}
