package modernmods.modernfoundry.tools.network;

import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class YoyoHandSyncPacket implements IThreadsafePacket {
  private final InteractionHand hand;
  private final ItemStack stack;

  public YoyoHandSyncPacket(InteractionHand hand, ItemStack stack) {
    this.hand = hand;
    this.stack = stack.copy();
  }

  public YoyoHandSyncPacket(FriendlyByteBuf buffer) {
    this.hand = buffer.readEnum(InteractionHand.class);
    this.stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(hand);
    ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, stack);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null) {
      player.setItemInHand(hand, stack);
    }
  }
}
