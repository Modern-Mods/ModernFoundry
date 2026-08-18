package modernmods.modernfoundry.fluids.util;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Fluid {@link net.neoforged.neoforge.transfer.ResourceHandler} for a container holding a constant fluid.
 * Draining swaps the container for its empty variant; it cannot be filled.
 */
public class ConstantFluidContainerWrapper extends ItemAccessResourceHandler<FluidResource> {
  /** Contained fluid resource */
  private final FluidResource fluid;
  /** Amount of the contained fluid */
  private final int amount;
  /** Resource representing the empty version of the container */
  private final ItemResource emptyResource;
  /** Item this handler was created for; if the item changes the handler reports empty */
  private final Item validItem;

  public ConstantFluidContainerWrapper(ItemAccess itemAccess, FluidStack fluid, ItemResource emptyResource) {
    super(itemAccess, 1);
    this.fluid = FluidResource.of(fluid);
    this.amount = fluid.getAmount();
    this.emptyResource = emptyResource;
    this.validItem = itemAccess.getResource().getItem();
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    return accessResource.is(validItem) ? fluid : FluidResource.EMPTY;
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    return accessResource.is(validItem) ? amount : 0;
  }

  @Override
  protected int getCapacity(int index, FluidResource resource) {
    return amount;
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    // only the constant fluid, and only while the backing item has not changed
    return itemAccess.getResource().is(validItem) && fluid.equals(resource);
  }

  @Override
  protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
    // fully drained: swap to the empty container
    if (newAmount == 0) {
      return emptyResource;
    }
    // still full and unchanged: keep the current container
    if (newAmount == amount && fluid.equals(newResource)) {
      return accessResource;
    }
    // any partial state is not a valid container (all-or-nothing)
    return ItemResource.EMPTY;
  }
}
