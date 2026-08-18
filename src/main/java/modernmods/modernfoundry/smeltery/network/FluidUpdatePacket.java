package modernmods.modernfoundry.smeltery.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.mantle.util.BlockEntityHelper;

public class FluidUpdatePacket implements IThreadsafePacket {
  /** Sentinel capacity meaning "the receiver already knows its capacity" (fixed-size tanks/channels) */
  public static final int NO_CAPACITY = -1;

  protected final BlockPos pos;
  protected final FluidStack fluid;
  /** Tank capacity, or {@link #NO_CAPACITY} when the sender has a fixed capacity the client already knows */
  protected final int capacity;

  public FluidUpdatePacket(BlockPos pos, FluidStack fluid) {
    this(pos, fluid, NO_CAPACITY);
  }

  /** Casting tanks size themselves from a recipe the client cannot look up, so they must sync their capacity here. */
  public FluidUpdatePacket(BlockPos pos, FluidStack fluid, int capacity) {
    this.pos = pos;
    this.fluid = fluid;
    this.capacity = capacity;
  }

  public FluidUpdatePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf)buffer);
    this.capacity = buffer.readVarInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf)buffer, fluid);
    buffer.writeVarInt(capacity);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Interface to implement for anything wishing to receive fluid updates */
  public interface IFluidPacketReceiver {

    /**
     * Updates the current fluid to the specified value
     *
     * @param fluid New fluidstack
     */
    void updateFluidTo(FluidStack fluid);

    /**
     * Updates the current fluid and, when the sender provides one, the tank capacity. Fixed-capacity tanks ignore the
     * capacity (it arrives as {@link #NO_CAPACITY}); casting tanks override this to apply the synced capacity, since the
     * client cannot recompute it from a recipe.
     *
     * @param fluid    New fluidstack
     * @param capacity Synced tank capacity, or {@link #NO_CAPACITY} to leave the receiver's capacity untouched
     */
    default void updateFluidTo(FluidStack fluid, int capacity) {
      updateFluidTo(fluid);
    }
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(FluidUpdatePacket packet) {
      BlockEntityHelper.get(IFluidPacketReceiver.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> te.updateFluidTo(packet.fluid, packet.capacity));
    }
  }
}
