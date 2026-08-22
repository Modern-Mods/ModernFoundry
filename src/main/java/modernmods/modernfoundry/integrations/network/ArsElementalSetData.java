package modernmods.modernfoundry.integrations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;

/** Server-authoritative Ars Elemental set projection. */
public final class ArsElementalSetData implements IThreadsafePacket {
  private final boolean air;
  private final boolean aqua;
  private final boolean earth;
  private final boolean fire;

  public ArsElementalSetData(boolean air, boolean aqua, boolean earth, boolean fire) {
    this.air = air;
    this.aqua = aqua;
    this.earth = earth;
    this.fire = fire;
  }

  public ArsElementalSetData(FriendlyByteBuf buffer) {
    air = buffer.readBoolean();
    aqua = buffer.readBoolean();
    earth = buffer.readBoolean();
    fire = buffer.readBoolean();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBoolean(air);
    buffer.writeBoolean(aqua);
    buffer.writeBoolean(earth);
    buffer.writeBoolean(fire);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null) {
      CapabilityRegistry.arsElemental(player).setAir(air);
      CapabilityRegistry.arsElemental(player).setAqua(aqua);
      CapabilityRegistry.arsElemental(player).setEarth(earth);
      CapabilityRegistry.arsElemental(player).setFire(fire);
    }
  }
}
