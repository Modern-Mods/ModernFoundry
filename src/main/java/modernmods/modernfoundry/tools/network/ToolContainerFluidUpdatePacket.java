package modernmods.modernfoundry.tools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements IThreadsafePacket {
  public ToolContainerFluidUpdatePacket(FriendlyByteBuf buffer) {
    this(FluidStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf)buffer));
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf)buffer, fluid);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null && player.containerMenu instanceof ToolContainerMenu toolMenu) {
      toolMenu.getTank().setFluid(fluid);
    }
  }
}
