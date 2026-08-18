package modernmods.modernfoundry.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Rail that drops items from a passing hopper minecart into the inventory below it.
 * The NeoForge onMinecartPass rail hook was removed in 26.1, so this reacts via {@link #entityInside}.
 * In 26.1 the item capability yields a {@link net.neoforged.neoforge.transfer.ResourceHandler ResourceHandler&lt;ItemResource&gt;};
 * we adapt it back to the legacy {@link IItemHandler} view via {@link IItemHandler#of} so the simulate/execute
 * extract-then-insert movement below keeps working unchanged.
 */
public class DropperRailBlock extends RailBlock {

  public DropperRailBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
    if (!(entity instanceof AbstractMinecart cart) || !(entity instanceof Hopper)) {
      return;
    }
    // pull the item handler off the minecart
    ResourceHandler<ItemResource> cartHandler = Capabilities.Item.ENTITY_AUTOMATION.getCapability(cart, Direction.UP);
    if (cartHandler == null) {
      return;
    }
    IItemHandler itemHandlerCart = IItemHandler.of(cartHandler);
    // find the inventory directly below the rail
    BlockEntity below = world.getBlockEntity(pos.below());
    if (below == null) {
      return;
    }
    ResourceHandler<ItemResource> belowHandler = world.getCapability(Capabilities.Item.BLOCK, pos.below(), below.getBlockState(), below, Direction.UP);
    if (belowHandler == null) {
      return;
    }
    IItemHandler itemHandlerTE = IItemHandler.of(belowHandler);

    for (int i = 0; i < itemHandlerCart.getSlots(); i++) {
      ItemStack itemStack = itemHandlerCart.extractItem(i, 1, true);
      if (itemStack.isEmpty()) {
        continue;
      }
      if (ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, true).isEmpty()) {
        itemStack = itemHandlerCart.extractItem(i, 1, false);
        ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, false);
        break;
      }
    }
  }

}
