package modernmods.modernfoundry.smeltery.block.entity.inventory;

import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.compat.neoforged.neoforge.common.ForgeHooks;
import modernmods.hilt.block.entity.HiltBlockEntity;
import modernmods.hilt.inventory.SingleItemHandler;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;

/**
 * Item handler holding the heater inventory
 */
public class HeaterItemHandler extends SingleItemHandler<HiltBlockEntity> {
  public HeaterItemHandler(HiltBlockEntity parent) {
    super(parent, 64);
  }

  @Override
  protected boolean isItemValid(ItemStack stack) {
    // fuel module divides by 4, so anything 3 or less is treated as 0
    return ForgeHooks.getBurnTime(stack, TinkerRecipeTypes.FUEL.get()) > 3;
  }
}
