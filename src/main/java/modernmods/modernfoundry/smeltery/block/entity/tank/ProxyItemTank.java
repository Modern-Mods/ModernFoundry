package modernmods.modernfoundry.smeltery.block.entity.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.mantle.inventory.SingleItemHandler;
import modernmods.mantle.util.RegistryHelper;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.network.InventorySlotSyncPacket;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.fluid.IFluidTankUpdater;

/**
 * Item handler for a block that stores a fluid-container item, exposing the contained item's fluid capability.
 * The item slot itself is a {@code ResourceHandler<ItemResource>} (via {@link SingleItemHandler}); the fluid side
 * is a separate {@code ResourceHandler<FluidResource>} obtained from {@link #getFluidHandler()}.
 */
public class ProxyItemTank<T extends MantleBlockEntity & IFluidTankUpdater> extends SingleItemHandler<T> {
  /** Item access bound to this slot, so fluid operations swap the stored container in place */
  private final ItemAccess fluidAccess = ItemAccess.forHandlerIndex(this, 0);
  /** Fluid handler proxying to the contained item */
  private final ResourceHandler<FluidResource> fluidHandler = new ProxyFluidHandler();

  public ProxyItemTank(T parent) {
    super(parent, 1);
  }

  /** Gets the fluid handler that proxies to the contained item */
  public ResourceHandler<FluidResource> getFluidHandler() {
    return fluidHandler;
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean isItemValid(ItemStack stack) {
    // can only store items that are fluid handlers, though allow blacklist in case something is really broken
    // blacklist is mostly used for items that don't support incremental filling, as this block really isn't good at working with them
    // we check the container item so we don't have to put every bucket in the tag. Not bothering with complex container items; odds are item stack sensitive just returns the same item
    ItemStackTemplate craftRemainder = stack.getItem().getCraftingRemainder(stack);
    return !stack.isEmpty()
      && !stack.is(TinkerTags.Items.PROXY_TANK_BLACKLIST)
      && (craftRemainder == null || !RegistryHelper.contains(TinkerTags.Items.PROXY_TANK_BLACKLIST, craftRemainder.item().value()))
      && Capabilities.Fluid.ITEM.getCapability(stack, ItemAccess.forStack(stack)) != null;
  }

  /** Sends the current slot contents to nearby clients, as we directly mutate the internal stack */
  private void syncToClients(ItemStack newStack) {
    Level world = parent.getLevel();
    if (world != null && !world.isClientSide()) {
      parent.onTankContentsChanged();
      BlockPos pos = parent.getBlockPos();
      TinkerNetwork.getInstance().sendToClientsAround(new InventorySlotSyncPacket(newStack, 0, pos), world, pos);
    }
  }

  @Override
  public void setStack(ItemStack newStack) {
    // if swapping to an empty stack, switch to the empty stack instance to prevent a 0-count capability
    if (newStack.isEmpty()) {
      newStack = ItemStack.EMPTY;
    }
    ItemStack oldStack = getStack();
    super.setStack(newStack);
    // if the stack instance actually changed contents, sync to clients
    if (oldStack != newStack && ((oldStack.isEmpty() && newStack.isEmpty()) || !ItemStack.isSameItemSameComponents(oldStack, newStack))) {
      syncToClients(newStack);
    }
  }

  @Override
  protected void onContentsChanged(int index, ItemStack previousContents) {
    // fires when the resource handler mutates the slot (e.g. fluid fill/drain swapping the container)
    super.onContentsChanged(index, previousContents);
    syncToClients(getStack());
  }

  /** Proxies fluid operations to the contained item's fluid capability */
  private class ProxyFluidHandler implements ResourceHandler<FluidResource> {
    /** Gets the fluid handler for the contained item, or an empty handler if none */
    private ResourceHandler<FluidResource> delegate() {
      ItemStack stack = getStack();
      if (stack.isEmpty()) {
        return EmptyResourceHandler.instance();
      }
      ResourceHandler<FluidResource> handler = Capabilities.Fluid.ITEM.getCapability(stack, fluidAccess);
      return handler == null ? EmptyResourceHandler.instance() : handler;
    }

    @Override
    public int size() {
      return delegate().size();
    }

    @Override
    public FluidResource getResource(int index) {
      return delegate().getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
      return delegate().getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
      return delegate().getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
      return delegate().isValid(index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
      return delegate().insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
      return delegate().extract(index, resource, amount, transaction);
    }
  }
}
