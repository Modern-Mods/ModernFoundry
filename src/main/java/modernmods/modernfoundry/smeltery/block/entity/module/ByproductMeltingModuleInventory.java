package modernmods.modernfoundry.smeltery.block.entity.module;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import modernmods.hilt.block.entity.HiltBlockEntity;
import modernmods.modernfoundry.library.recipe.melting.IMeltingContainer.IOreRate;
import modernmods.modernfoundry.library.recipe.melting.IMeltingRecipe;

public class ByproductMeltingModuleInventory extends MeltingModuleInventory {
  public ByproductMeltingModuleInventory(HiltBlockEntity parent, IFluidHandler fluidHandler, IOreRate oreRate, int size) {
    super(parent, fluidHandler, oreRate, size);
  }

  public ByproductMeltingModuleInventory(HiltBlockEntity parent, IFluidHandler fluidHandler, IOreRate oreRate) {
    super(parent, fluidHandler, oreRate);
  }

  @Override
  protected boolean tryFillTank(int index, IMeltingRecipe recipe) {
    if (super.tryFillTank(index, recipe)) {
      recipe.handleByproducts(getModule(index), fluidHandler);
      return true;
    }
    return false;
  }
}
