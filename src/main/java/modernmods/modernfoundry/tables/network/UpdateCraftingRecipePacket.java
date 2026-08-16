package modernmods.modernfoundry.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.hilt.util.BlockEntityHelper;
import modernmods.modernfoundry.tables.block.entity.table.CraftingStationBlockEntity;

/**
 * Packet to send the current crafting recipe to a player who opens the crafting station
 */
public class UpdateCraftingRecipePacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final ResourceLocation recipe;
  public UpdateCraftingRecipePacket(BlockPos pos, RecipeHolder<CraftingRecipe> recipe) {
    this.pos = pos;
    this.recipe = recipe.id();
  }

  public UpdateCraftingRecipePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.recipe = buffer.readResourceLocation();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeResourceLocation(recipe);
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
        BlockEntityHelper.get(CraftingStationBlockEntity.class, world, packet.pos).ifPresent(te ->
          world.getRecipeManager().byKey(packet.recipe).filter(recipe -> recipe.value() instanceof CraftingRecipe).map(recipe -> (RecipeHolder<CraftingRecipe>)(RecipeHolder<?>)recipe).ifPresent(te::updateRecipe));
      }
    }
  }
}
