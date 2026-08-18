package modernmods.modernfoundry.tables.block.entity.chest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.tables.TinkerTables;
import modernmods.modernfoundry.tables.block.entity.inventory.ScalingChestItemHandler;

/**
 * Chest that holds parts, up to 64 of a given material and type
 */
public class PartChestBlockEntity extends AbstractChestBlockEntity {
  private static final Component NAME = TConstruct.makeTranslation("gui", "part_chest");
  public PartChestBlockEntity(BlockPos pos, BlockState state) {
    super(TinkerTables.partChestTile.get(), pos, state, NAME, new PartChestItemHandler());
  }

  /** Item handler for part chests */
  public static class PartChestItemHandler extends ScalingChestItemHandler {
    @Override
    public int getSlotLimit(int slot) {
      return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      // check if there is no other slot containing that item
      for (int i = 0; i < this.getSlots(); i++) {
        // don't compare count
        if (ItemStack.isSameItemSameComponents(stack, this.getStackInSlot(i))) {
          return i == slot; // only allowed in the same slot
        }
      }
      return stack.is(TinkerTags.Items.CHEST_PARTS);
    }
  }
}
