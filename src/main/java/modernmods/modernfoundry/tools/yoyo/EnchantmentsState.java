package modernmods.modernfoundry.tools.yoyo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class EnchantmentsState {
  private static final Codec<Object2BooleanArrayMap<ResourceKey<Enchantment>>> ENCHANTMENTS_CODEC = Codec.unboundedMap(
    ResourceKey.codec(Registries.ENCHANTMENT), Codec.BOOL
  ).xmap(Object2BooleanArrayMap::new, Function.identity());
  public static final Codec<EnchantmentsState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    ENCHANTMENTS_CODEC.fieldOf("enchantments").forGetter(state -> state.enchantments)
  ).apply(instance, EnchantmentsState::new));
  public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentsState> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.map(Object2BooleanArrayMap::new, ResourceKey.streamCodec(Registries.ENCHANTMENT), ByteBufCodecs.BOOL),
    state -> state.enchantments, EnchantmentsState::new
  );
  public static final EnchantmentsState EMPTY = new EnchantmentsState(new Object2BooleanArrayMap<>());

  private final Object2BooleanArrayMap<ResourceKey<Enchantment>> enchantments;

  public EnchantmentsState(Object2BooleanArrayMap<ResourceKey<Enchantment>> enchantments) {
    this.enchantments = enchantments;
  }

  public EnchantmentsState copy() {
    return new EnchantmentsState(new Object2BooleanArrayMap<>(enchantments));
  }

  public boolean isEnchantmentActivate(ItemStack stack, ResourceKey<Enchantment> enchantment, Provider provider) {
    if (!(stack.getItem() instanceof YoyoItem)) return false;
    if (enchantment.equals(YoyoEnchantments.COLLECTING)) {
      return ((YoyoItem) stack.getItem()).getMaxCollectedDrops(stack, provider) > 0
        && (!enchantments.containsKey(enchantment) || enchantments.getBoolean(enchantment));
    }
    return stack.getEnchantmentLevel(provider.holderOrThrow(enchantment)) > 0
      && (!enchantment.equals(YoyoEnchantments.CRAFTING) && !enchantments.containsKey(enchantment) || enchantments.getBoolean(enchantment))
      && checkEnchantmentCompat(stack, enchantment, provider);
  }

  private boolean checkEnchantmentCompat(ItemStack stack, ResourceKey<Enchantment> enchantment, Provider provider) {
    if (enchantment.equals(YoyoEnchantments.BREAKING)) return !isEnchantmentActivate(stack, YoyoEnchantments.CRAFTING, provider);
    if (enchantment.equals(YoyoEnchantments.CRAFTING)) return !isEnchantmentActivate(stack, YoyoEnchantments.BREAKING, provider);
    return true;
  }

  public boolean toggleEnchantment(ResourceKey<Enchantment> enchantment, ItemStack stack, Provider provider) {
    boolean enabled = !isEnchantmentActivate(stack, enchantment, provider);
    enchantments.removeBoolean(enchantment);
    enchantments.put(enchantment, enabled);
    return enabled;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof EnchantmentsState state && enchantments.equals(state.enchantments);
  }

  @Override
  public int hashCode() {
    return 31 * enchantments.hashCode();
  }
}
