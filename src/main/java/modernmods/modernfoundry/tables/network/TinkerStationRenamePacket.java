package modernmods.modernfoundry.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.tables.block.entity.table.TinkerStationBlockEntity;
import modernmods.modernfoundry.tables.menu.TinkerStationContainerMenu;

/** Packet to send to the server to update the name in the UI */
@RequiredArgsConstructor
public class TinkerStationRenamePacket implements IThreadsafePacket {
  private final String name;

  public TinkerStationRenamePacket(FriendlyByteBuf buf) {
    this.name = buf.readUtf(Short.MAX_VALUE);
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeUtf(name);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ServerPlayer sender = (context.player() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
    if (sender != null && sender.containerMenu instanceof TinkerStationContainerMenu station) {
      TinkerStationBlockEntity tile = station.getTile();
      if (tile != null) {
        station.getTile().setItemName(name);
      }
    }
  }
}
