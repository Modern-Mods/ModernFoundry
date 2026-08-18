package modernmods.modernfoundry.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.mantle.recipe.helper.RecipeHelper;
import modernmods.mantle.util.BlockEntityHelper;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.tables.client.inventory.TinkerStationScreen;
import modernmods.modernfoundry.tables.block.entity.table.TinkerStationBlockEntity;

import java.util.Optional;

/**
 * Packet to send the current crafting recipe to a player who opens the tinker station
 */
public class UpdateTinkerStationRecipePacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final Identifier recipe;
  public UpdateTinkerStationRecipePacket(BlockPos pos, ITinkerStationRecipe recipe) {
    this.pos = pos;
    this.recipe = recipe.getId();
  }

  public UpdateTinkerStationRecipePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.recipe = buffer.readIdentifier();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeIdentifier(recipe);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(UpdateTinkerStationRecipePacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        // 26.1: the full RecipeManager is no longer synced to the client (only a limited RecipeAccess), so the recipe
        // cannot be resolved here as it was pre-26.1 (the old world.getServer().getRecipeManager() NPE'd on the client).
        // The result is now synced straight into the result slot via the container (LazyResultContainer.setItem); this
        // packet just refreshes the open station screen so it re-reads that synced result.
        if (Minecraft.getInstance().screen instanceof TinkerStationScreen stationScreen) {
          TinkerStationBlockEntity te = stationScreen.getTileEntity();
          if (te.getBlockPos().equals(packet.pos)) {
            stationScreen.updateDisplay();
          }
        }
      }
    }
  }
}
