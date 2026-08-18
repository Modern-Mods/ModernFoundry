package modernmods.modernfoundry.library.tools.definition;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;

import java.util.Map;
import java.util.Map.Entry;

/** Packet to sync tool definitions to the client */
@RequiredArgsConstructor
public class UpdateToolDefinitionDataPacket implements IThreadsafePacket {
  @Getter(AccessLevel.PROTECTED)
  private final Map<Identifier, ToolDefinitionData> dataMap;

  public UpdateToolDefinitionDataPacket(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    ImmutableMap.Builder<Identifier, ToolDefinitionData> builder = ImmutableMap.builder();
    for (int i = 0; i < size; i++) {
      Identifier name = buffer.readIdentifier();
      ToolDefinitionData data = ToolDefinitionData.LOADABLE.decode(buffer, ToolDefinitionLoader.contextBuilder(name).build());
      builder.put(name, data);
    }
    dataMap = builder.build();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(dataMap.size());
    for (Entry<Identifier, ToolDefinitionData> entry : dataMap.entrySet()) {
      buffer.writeIdentifier(entry.getKey());
      ToolDefinitionData.LOADABLE.encode(buffer, entry.getValue());
    }
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ToolDefinitionLoader.getInstance().updateDataFromServer(dataMap);
  }
}
