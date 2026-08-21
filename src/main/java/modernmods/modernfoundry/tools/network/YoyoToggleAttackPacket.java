package modernmods.modernfoundry.tools.network;

import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class YoyoToggleAttackPacket implements IThreadsafePacket {
  private final InteractionHand hand;

  public YoyoToggleAttackPacket(InteractionHand hand) { this.hand = hand; }
  public YoyoToggleAttackPacket(FriendlyByteBuf buffer) { hand = buffer.readEnum(InteractionHand.class); }

  @Override
  public void encode(FriendlyByteBuf buffer) { buffer.writeEnum(hand); }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    if (context.player() instanceof ServerPlayer player && player.getItemInHand(hand).getItem() instanceof YoyoItem) {
      ItemStack stack = player.getItemInHand(hand);
      YoyoItem.toggleAttack(stack);
    }
  }
}
