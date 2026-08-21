package modernmods.modernfoundry.tools.yoyo;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public final class YoyoEnchantments {
  private YoyoEnchantments() {}

  public static final ResourceKey<Enchantment> COLLECTING = key("collecting");
  public static final ResourceKey<Enchantment> BREAKING = key("breaking");
  public static final ResourceKey<Enchantment> CRAFTING = key("crafting");

  private static ResourceKey<Enchantment> key(String id) {
    return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(TConstruct.MOD_ID, id));
  }
}
