package modernmods.modernfoundry.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.modernfoundry.compat.neoforged.neoforge.network.NetworkHooks;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.tables.block.ITabbedBlock;

@RequiredArgsConstructor
public class StationTabPacket implements IThreadsafePacket {
  private final BlockPos pos;

  public StationTabPacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ServerPlayer sender = (context.player() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
    if (sender != null) {
      ItemStack heldStack = sender.containerMenu.getCarried();
      if (!heldStack.isEmpty()) {
        // set it to empty, so it's doesn't get dropped
        sender.containerMenu.setCarried(ItemStack.EMPTY);
      }

      Level world = sender.getCommandSenderWorld();
      if (!world.hasChunkAt(pos)) {
        return;
      }
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() instanceof ITabbedBlock) {
        ((ITabbedBlock) state.getBlock()).openGui(sender, sender.getCommandSenderWorld(), pos);
      } else {
        MenuProvider provider = state.getMenuProvider(sender.getCommandSenderWorld(), pos);
        if (provider != null) {
          NetworkHooks.openScreen(sender, provider, pos);
        }
      }

      if (!heldStack.isEmpty()) {
        sender.containerMenu.setCarried(heldStack);
        TinkerNetwork.getInstance().sendVanillaPacket(sender, new ClientboundContainerSetSlotPacket(-1, -1, -1, heldStack));
      }
    }
  }
}
