package modernmods.modernfoundry.library.modifiers;

import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.utils.IdParser;
import modernmods.modernfoundry.library.utils.ResourceId;

import javax.annotation.Nullable;

/**
 * This is just a copy of Identifier for type safety in modifier JSON.
 */
public class ModifierId extends ResourceId {
  public static final IdParser<ModifierId> PARSER = new IdParser<>(ModifierId::new, "Modifier");
  /** ID of the default modifier. Used in a few contexts to indicate "no modifier" instead of using null. */
  public static final ModifierId EMPTY = new ModifierId(TConstruct.MOD_ID, "empty");
  /**
   * Context key used in {@link modernmods.modernfoundry.library.client.modifiers.ModifierModelMapManager}.
   * Note when the modifier itself is the JSON file (such as modifier JSON), {@link ContextKey#ID} will be used instead.
   */
  public static final ContextKey<ModifierId> CONTEXT_KEY = new ContextKey<>("modifier");

  public ModifierId(String resourceName) {
    super(resourceName);
  }

  public ModifierId(String namespaceIn, String pathIn) {
    super(namespaceIn, pathIn);
  }

  public ModifierId(Identifier location) {
    super(location);
  }

  /** {@return Modifier ID, or null if invalid} */
  @Nullable
  public static ModifierId tryParse(String string) {
    return tryParse(string, ModifierId::new);
  }

  /** {@return Modifier ID, or null if invalid} */
  @Nullable
  public static ModifierId tryBuild(String namespace, String path) {
    return tryBuild(namespace, path, ModifierId::new);
  }
}
