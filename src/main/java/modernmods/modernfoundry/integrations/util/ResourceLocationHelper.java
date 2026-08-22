package modernmods.modernfoundry.integrations.util;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.resources.ResourceLocation;

/** Native resource helpers; imported integration content belongs to Modern Foundry. */
public final class ResourceLocationHelper {
  private ResourceLocationHelper() {}

  public static ResourceLocation resource(String path) {
    return TConstruct.getResource(path);
  }

  public static ResourceLocation location(String namespace, String path) {
    return ResourceLocation.fromNamespaceAndPath(namespace, path);
  }
}
