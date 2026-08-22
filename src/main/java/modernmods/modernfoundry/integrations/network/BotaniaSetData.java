package modernmods.modernfoundry.integrations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;

/** Server-authoritative Botania set projection. */
public final class BotaniaSetData implements IThreadsafePacket {
  private final boolean terrestrial;
  private final boolean greatFairy;
  private final boolean alfheim;

  public BotaniaSetData(boolean terrestrial, boolean greatFairy, boolean alfheim) {
    this.terrestrial = terrestrial;
    this.greatFairy = greatFairy;
    this.alfheim = alfheim;
  }

  public BotaniaSetData(FriendlyByteBuf buffer) {
    terrestrial = buffer.readBoolean();
    greatFairy = buffer.readBoolean();
    alfheim = buffer.readBoolean();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBoolean(terrestrial);
    buffer.writeBoolean(greatFairy);
    buffer.writeBoolean(alfheim);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null) {
      var data = CapabilityRegistry.botania(player);
      data.setTerrestrial(terrestrial);
      data.setGreatFairy(greatFairy);
      data.setAlfheim(alfheim);
    }
  }
}
