package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;

public final class YoyoRetractPacket implements IThreadsafePacket {
  private final int yoyoId;

  public YoyoRetractPacket(YoyoEntity yoyo) {
    yoyoId = yoyo.getId();
  }

  public YoyoRetractPacket(FriendlyByteBuf buffer) {
    yoyoId = buffer.readVarInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(yoyoId);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Level level = SafeClientAccess.getLevel();
    if (level != null && level.getEntity(yoyoId) instanceof YoyoEntity yoyo) yoyo.getController().signalRetract();
  }
}
