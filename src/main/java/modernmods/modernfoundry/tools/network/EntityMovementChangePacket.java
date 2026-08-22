package modernmods.modernfoundry.tools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;

public class EntityMovementChangePacket implements IThreadsafePacket {
  private final int entityID;
  private final double x;
  private final double y;
  private final double z;
  private final float yRot;
  private final float xRot;

  public EntityMovementChangePacket(Entity entity) {
    this.entityID = entity.getId();
    this.x = entity.getDeltaMovement().x;
    this.y = entity.getDeltaMovement().y;
    this.z = entity.getDeltaMovement().z;
    this.yRot = entity.getYRot();
    this.xRot = entity.getXRot();
  }

  public EntityMovementChangePacket(FriendlyByteBuf buffer) {
    this.entityID = buffer.readInt();
    this.x = buffer.readDouble();
    this.y = buffer.readDouble();
    this.z = buffer.readDouble();
    this.yRot = buffer.readFloat();
    this.xRot = buffer.readFloat();
  }

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {
    packetBuffer.writeInt(this.entityID);
    packetBuffer.writeDouble(this.x);
    packetBuffer.writeDouble(this.y);
    packetBuffer.writeDouble(this.z);
    packetBuffer.writeFloat(this.yRot);
    packetBuffer.writeFloat(this.xRot);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    if ((context.player() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null) != null) {
      HandleClient.handle(this);
    }
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(EntityMovementChangePacket packet) {
      Level level = SafeClientAccess.getLevel();
      Entity entity = level == null ? null : level.getEntity(packet.entityID);
      if (entity != null) {
        entity.setDeltaMovement(packet.x, packet.y, packet.z);
        entity.setYRot(packet.yRot);
        entity.setXRot(packet.xRot);
      }
    }
  }
}
