package modernmods.modernfoundry.thinking.common.networking.packet.packet;
import modernmods.modernfoundry.thinking.common.things.block.entity.DryingRackBlockEntity;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ItemStackSyncS2CPacket implements IThreadsafePacket {
    private final ItemStackHandler itemStackHandler;
    private final BlockPos pos;

    public ItemStackSyncS2CPacket(ItemStackHandler itemStackHandler, BlockPos pos) {
        this.itemStackHandler = itemStackHandler;
        this.pos = pos;
    }

    public ItemStackSyncS2CPacket(FriendlyByteBuf buf) {
        itemStackHandler = new ItemStackHandler(buf.readVarInt());
        for(int i = 0; i < itemStackHandler.getSlots(); i++){
            itemStackHandler.setStackInSlot(i, ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf));
        }
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeVarInt(itemStackHandler.getSlots());
        for(int i = 0; i < itemStackHandler.getSlots(); i++){
            ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, itemStackHandler.getStackInSlot(i));
        }
        buf.writeBlockPos(pos);
    }

    @Override
    public void handleThreadsafe(IPayloadContext context){
        if (SafeClientAccess.getLevel() != null && SafeClientAccess.getLevel().getBlockEntity(pos) instanceof DryingRackBlockEntity blockEntity) {
            blockEntity.setHandler(this.itemStackHandler);
        }
    }
}
