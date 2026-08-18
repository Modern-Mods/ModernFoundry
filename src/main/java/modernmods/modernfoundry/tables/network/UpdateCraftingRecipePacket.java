package modernmods.modernfoundry.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.mantle.util.BlockEntityHelper;
import modernmods.modernfoundry.tables.block.entity.table.CraftingStationBlockEntity;

/**
 * Packet to send the current crafting recipe to a player who opens the crafting station
 */
public class UpdateCraftingRecipePacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final Identifier recipe;
  public UpdateCraftingRecipePacket(BlockPos pos, RecipeHolder<CraftingRecipe> recipe) {
    this.pos = pos;
    this.recipe = recipe.id().identifier();
  }

  public UpdateCraftingRecipePacket(FriendlyByteBuf buffer) {
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
    private static void handle(UpdateCraftingRecipePacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        // 26.1: the full RecipeManager is no longer synced to the client, so the recipe cannot be resolved by key here
        // (the old world.getServer().getRecipeManager() NPE'd on the client). The crafting station's visible result comes
        // from its real synced ResultContainer, so nothing further is needed here for the result to display.
      }
    }
  }
}
