package modernmods.modernfoundry.common.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;

/** Packet to sync player persistent data to the client */
@RequiredArgsConstructor
public class SyncPersistentDataPacket implements IThreadsafePacket {
  private final CompoundTag data;

  public SyncPersistentDataPacket(FriendlyByteBuf buffer) {
    data = buffer.readNbt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeNbt(data);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Handles client side only code safely */
  private static class HandleClient {
    private static void handle(SyncPersistentDataPacket packet) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
        PersistentDataCapability.getCapability(player).ifPresent(data -> data.copyFrom(packet.data));
      }
    }
  }
}
