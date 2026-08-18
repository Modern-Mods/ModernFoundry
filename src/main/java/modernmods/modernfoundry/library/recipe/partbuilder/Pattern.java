package modernmods.modernfoundry.library.recipe.partbuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.library.utils.IdParser;
import modernmods.modernfoundry.library.utils.ResourceId;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;

/**
 * Resource location variant for pattern buttons in the part builder. Also used in a few spots for convenient icon loading.
 */
public class Pattern extends ResourceId {
  public static final IdParser<Pattern> PARSER = new IdParser<>(Pattern::new, "Pattern");

  public Pattern(String resourceName) {
    super(resourceName);
  }

  public Pattern(String namespaceIn, String pathIn) {
    super(namespaceIn, pathIn);
  }

  public Pattern(Identifier location) {
    super(location);
  }

  /**
   * Gets the translation key for this pattern
   * @return  Translation key
   */
  public String getTranslationKey() {
    return Util.makeTranslationKey("pattern", getIdentifier());
  }

  /**
   * Gets the display name for this pattern
   * @return  Display name
   */
  public Component getDisplayName() {
    return Component.translatable(getTranslationKey());
  }

  /**
   * Gets the texture for this pattern for rendering
   * @return  Pattern texture
   */
  public Identifier getTexture() {
    return Identifier.fromNamespaceAndPath(getNamespace(), "gui/tinker_pattern/" + getPath());
  }


  /** {@return Pattern ID, or null if invalid} */
  @Nullable
  public static Pattern tryParse(String string) {
    return tryParse(string, Pattern::new);
  }

  /** {@return Pattern ID, or null if invalid} */
  @Nullable
  public static Pattern tryBuild(String namespace, String path) {
    return tryBuild(namespace, path, Pattern::new);
  }
}
