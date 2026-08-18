package modernmods.modernfoundry.smeltery.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import modernmods.modernfoundry.library.fluid.SimpleFluidResourceTank;
import modernmods.modernfoundry.smeltery.block.entity.component.TankBlockEntity;

/**
 * Fluid handler that works with a tank item to adjust its tank in NBT.
 * Values are stored per-item; the base handler scales them by the stack size.
 */
public class TankItemFluidHandler extends ItemAccessResourceHandler<FluidResource> {
  /** Item this handler was created for; if the item changes the handler reports empty */
  private final Item validItem;

  public TankItemFluidHandler(ItemAccess itemAccess) {
    super(itemAccess, 1);
    this.validItem = itemAccess.getResource().getItem();
  }

  /** Reads the per-item tank from the item */
  private static SimpleFluidResourceTank readTank(ItemResource accessResource) {
    return TankItem.getTank(accessResource.toStack(), 1);
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    if (!accessResource.is(validItem)) {
      return FluidResource.EMPTY;
    }
    return FluidResource.of(readTank(accessResource).getFluid());
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    if (!accessResource.is(validItem)) {
      return 0;
    }
    return readTank(accessResource).getFluidAmount();
  }

  @Override
  protected int getCapacity(int index, FluidResource resource) {
    return TankBlockEntity.getCapacity(validItem);
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
    ItemStack stack = accessResource.toStack();
    SimpleFluidResourceTank tank = TankItem.getTank(stack, 1);
    tank.setFluid(newAmount == 0 ? FluidStack.EMPTY : newResource.toStack(newAmount));
    TankItem.setTank(stack, tank);
    return ItemResource.of(stack);
  }
}
