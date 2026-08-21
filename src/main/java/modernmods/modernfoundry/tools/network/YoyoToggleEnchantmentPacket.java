package modernmods.modernfoundry.tools.network;

import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.tools.yoyo.YoyoEnchantments;
import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class YoyoToggleEnchantmentPacket implements IThreadsafePacket {
  private final InteractionHand hand;
  private final ResourceLocation enchantment;

  public YoyoToggleEnchantmentPacket(InteractionHand hand, ResourceLocation enchantment) {
    this.hand = hand;
    this.enchantment = enchantment;
  }

  public YoyoToggleEnchantmentPacket(FriendlyByteBuf buffer) {
    hand = buffer.readEnum(InteractionHand.class);
    enchantment = buffer.readResourceLocation();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(hand);
    buffer.writeResourceLocation(enchantment);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    if (!(context.player() instanceof ServerPlayer player) || !(player.getItemInHand(hand).getItem() instanceof YoyoItem)) return;
    ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantment);
    if (!enchantment.getNamespace().equals(TConstruct.MOD_ID)
      || !(key.equals(YoyoEnchantments.COLLECTING) || key.equals(YoyoEnchantments.BREAKING) || key.equals(YoyoEnchantments.CRAFTING))) return;
    ItemStack stack = player.getItemInHand(hand);
    YoyoItem.toggleEnchant(stack, key, player.registryAccess(), player);
  }
}
