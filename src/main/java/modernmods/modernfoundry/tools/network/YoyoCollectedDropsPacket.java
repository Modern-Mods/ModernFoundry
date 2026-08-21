package modernmods.modernfoundry.tools.network;

import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class YoyoCollectedDropsPacket implements IThreadsafePacket {
  private final int yoyoId;
  private final List<ItemStack> drops;

  public YoyoCollectedDropsPacket(YoyoEntity yoyo) {
    this(yoyo.getId(), yoyo.getCollectedDrops());
  }

  public YoyoCollectedDropsPacket(int yoyoId, List<ItemStack> drops) {
    this.yoyoId = yoyoId;
    this.drops = drops.stream().map(ItemStack::copy).toList();
  }

  public YoyoCollectedDropsPacket(FriendlyByteBuf buffer) {
    yoyoId = buffer.readVarInt();
    int size = buffer.readVarInt();
    drops = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      drops.add(ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer));
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(yoyoId);
    buffer.writeVarInt(drops.size());
    for (ItemStack drop : drops) ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, drop);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Level level = SafeClientAccess.getLevel();
    if (level != null && level.getEntity(yoyoId) instanceof YoyoEntity yoyo) yoyo.setCollectedDrops(drops);
  }
}
