package modernmods.modernfoundry.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.library.tools.layout.StationSlotLayoutLoader;
import modernmods.modernfoundry.tables.menu.TinkerStationContainerMenu;

@RequiredArgsConstructor
public class TinkerStationSelectionPacket implements IThreadsafePacket {
  private final Identifier layoutName;
  public TinkerStationSelectionPacket(FriendlyByteBuf buffer) {
    this.layoutName = buffer.readIdentifier();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeIdentifier(this.layoutName);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ServerPlayer sender = (context.player() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
    if (sender != null) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof TinkerStationContainerMenu tinker) {
        tinker.setToolSelection(StationSlotLayoutLoader.getInstance().get(layoutName));
      }
    }
  }
}
