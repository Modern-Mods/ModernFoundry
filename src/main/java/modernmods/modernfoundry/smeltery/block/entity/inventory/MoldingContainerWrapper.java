package modernmods.modernfoundry.smeltery.block.entity.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import modernmods.modernfoundry.library.recipe.molding.IMoldingContainer;

/** Wrapper around an item handler for the sake of use as a molding inventory */
@RequiredArgsConstructor
public class MoldingContainerWrapper implements IMoldingContainer {
  private final IItemHandler handler;
  private final int slot;

  @Getter @Setter
  private ItemStack pattern = ItemStack.EMPTY;

  @Override
  public ItemStack getMaterial() {
    return handler.getStackInSlot(slot);
  }
}
