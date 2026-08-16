package modernmods.modernfoundry.smeltery.network;

import lombok.AllArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.inventory.BaseContainerMenu;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.smeltery.block.entity.tank.ISmelteryTankHandler;

/**
 * Packet sent when a fluid is clicked in the smeltery UI
 */
@AllArgsConstructor
public class SmelteryFluidClickedPacket implements IThreadsafePacket {
  private final int index;

  public SmelteryFluidClickedPacket(FriendlyByteBuf buffer) {
    index = buffer.readVarInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(index);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ServerPlayer sender = (context.player() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
    if (sender != null && !sender.isSpectator()) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof BaseContainerMenu<?> base && base.getTile() instanceof ISmelteryTankHandler tank) {
        tank.getTank().moveFluidToBottom(index);
      }
    }
  }
}
