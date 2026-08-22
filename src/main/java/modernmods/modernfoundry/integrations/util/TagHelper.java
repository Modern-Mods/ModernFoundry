package modernmods.modernfoundry.integrations.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Small adapter for the 1.21 holder-set tag API. */
public final class TagHelper {
  private TagHelper() {}

  public static Iterable<Holder<Item>> getTag(ResourceLocation loc) {
    return getTag(TagKey.create(Registries.ITEM, loc));
  }

  public static Iterable<Holder<Item>> getTag(TagKey<Item> name) {
    return BuiltInRegistries.ITEM.getTagOrEmpty(name);
  }
}
