package modernmods.modernfoundry.tools.network;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.common.config.Config;

/** Client projection of a server-authoritative tool level-up event. */
public class LevelUpPacket implements IThreadsafePacket {
  private final int level;
  private final Component toolName;

  public LevelUpPacket(int level, Component toolName) {
    this.level = level;
    this.toolName = toolName;
  }

  public LevelUpPacket(FriendlyByteBuf buffer) {
    this.level = buffer.readVarInt();
    this.toolName = ComponentSerialization.TRUSTED_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(level);
    ComponentSerialization.TRUSTED_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, toolName);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(LevelUpPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
        return;
      }
      if (Config.CLIENT.improvableLevelUpMessage.get()) {
        minecraft.player.displayClientMessage(Component.translatable("message.modernfoundry.level_up",
          packet.toolName, packet.level).withStyle(ChatFormatting.GREEN), false);
      }
      minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
    }
  }
}
