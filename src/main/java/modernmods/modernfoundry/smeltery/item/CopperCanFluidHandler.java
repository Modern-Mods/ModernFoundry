package modernmods.modernfoundry.smeltery.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.utils.TagUtil;

/** Fluid handler for the copper can item; holds exactly one ingot of fluid (all-or-nothing) per can. */
public class CopperCanFluidHandler extends ItemAccessResourceHandler<FluidResource> {
  /** Item this handler was created for; if the item changes the handler reports empty */
  private final Item validItem;

  public CopperCanFluidHandler(ItemAccess itemAccess) {
    super(itemAccess, 1);
    this.validItem = itemAccess.getResource().getItem();
  }

  /** Reads the contained fluid stack (one ingot) from the item, or empty if none */
  private FluidStack readFluid(ItemResource accessResource) {
    if (!accessResource.is(validItem)) {
      return FluidStack.EMPTY;
    }
    ItemStack stack = accessResource.toStack();
    Fluid fluid = CopperCanItem.getFluid(stack);
    if (fluid == Fluids.EMPTY) {
      return FluidStack.EMPTY;
    }
    return TagUtil.createFluidStack(fluid, FluidValues.INGOT, CopperCanItem.getFluidTag(stack));
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    return FluidResource.of(readFluid(accessResource));
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    return readFluid(accessResource).getAmount();
  }

  @Override
  protected int getCapacity(int index, FluidResource resource) {
    return FluidValues.INGOT;
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    return itemAccess.getResource().is(validItem);
  }

  @Override
  protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
    if (!accessResource.is(validItem)) {
      return ItemResource.EMPTY;
    }
    // only whole-ingot states are valid (all-or-nothing, no partial fills)
    if (newAmount != 0 && newAmount != FluidValues.INGOT) {
      return ItemResource.EMPTY;
    }
    ItemStack stack = accessResource.toStack();
    if (newAmount == 0) {
      CopperCanItem.setFluid(stack, FluidStack.EMPTY);
    } else {
      CopperCanItem.setFluid(stack, newResource.toStack(FluidValues.INGOT));
    }
    return ItemResource.of(stack);
  }
}
