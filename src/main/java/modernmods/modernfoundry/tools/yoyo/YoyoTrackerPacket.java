package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;

public final class YoyoTrackerPacket implements IThreadsafePacket {
  private final int parentId;
  private final int mainHandYoyoId;
  private final int offHandYoyoId;

  YoyoTrackerPacket(YoyoTracker tracker) {
    parentId = tracker.parentId();
    mainHandYoyoId = tracker.yoyoId(net.minecraft.world.InteractionHand.MAIN_HAND);
    offHandYoyoId = tracker.yoyoId(net.minecraft.world.InteractionHand.OFF_HAND);
  }

  public YoyoTrackerPacket(FriendlyByteBuf buffer) {
    parentId = buffer.readVarInt();
    mainHandYoyoId = buffer.readVarInt();
    offHandYoyoId = buffer.readVarInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(parentId);
    buffer.writeVarInt(mainHandYoyoId);
    buffer.writeVarInt(offHandYoyoId);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Level level = SafeClientAccess.getLevel();
    if (level == null || !(level.getEntity(parentId) instanceof LivingEntity living)) return;
    YoyoTracker.on(living).apply(id -> level.getEntity(id) instanceof YoyoEntity yoyo ? yoyo : null, mainHandYoyoId, offHandYoyoId);
  }
}
