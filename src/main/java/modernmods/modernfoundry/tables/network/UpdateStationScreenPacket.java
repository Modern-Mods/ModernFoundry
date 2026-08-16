package modernmods.modernfoundry.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.tables.client.inventory.BaseTabbedScreen;

public class UpdateStationScreenPacket implements IThreadsafePacket {
  public static final UpdateStationScreenPacket INSTANCE = new UpdateStationScreenPacket();

  private UpdateStationScreenPacket() {}

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {}

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle();
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle() {
      Screen screen = Minecraft.getInstance().screen;
      if (screen != null) {
        if (screen instanceof BaseTabbedScreen) {
          ((BaseTabbedScreen<?,?>) screen).updateDisplay();
        }
      }
    }
  }
}
