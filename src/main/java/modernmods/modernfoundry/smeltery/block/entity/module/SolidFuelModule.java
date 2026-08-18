package modernmods.modernfoundry.smeltery.block.entity.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import modernmods.modernfoundry.compat.neoforged.neoforge.common.ForgeHooks;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import java.util.function.Consumer;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.mantle.inventory.EmptyItemHandler;
import modernmods.mantle.util.WeakConsumerWrapper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;
import modernmods.modernfoundry.library.recipe.fuel.MeltingFuel;
import modernmods.modernfoundry.library.recipe.fuel.MeltingFuelLookup;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;

/** Fuel module variant that supports both item and fluid fuels. Only supports a single fluid position which should not change. */
public class SolidFuelModule extends FuelModule {
  /** Listener to attach to stored item capabilities */
  private final Consumer<LazyOptional<IItemHandler>> itemListener = new WeakConsumerWrapper<>(this, SolidFuelModule::resetHandler);

  /** Location of the fuel tank */
  private final BlockPos fuelPos;
  /** Last item handler where items were extracted */
  @Nullable
  private LazyOptional<IItemHandler> itemHandler;
  /** Raw fluid resource handler for the fuel tank, exposed to menus */
  @Nullable
  private ResourceHandler<FluidResource> fluidTank;

  public SolidFuelModule(MantleBlockEntity parent, BlockPos fuelPos) {
    super(parent);
    this.fuelPos = fuelPos;
  }

  @Override
  protected void resetHandler(@Nullable LazyOptional<?> source) {
    // if the source is either of our handlers, clear both listeners to ensure cleanest refetc
    if (source == null || source == itemHandler || source == fluidHandler) {
      // remove listeners for efficiency, but we have to skip removing the listener that caused this
      if (Util.isForge()) {
        if (itemHandler != null && itemHandler != source) {
          itemHandler.removeListener(itemListener);
        }
        if (fluidHandler != null && fluidHandler != source) {
          fluidHandler.removeListener(fluidListener);
        }
      }
      itemHandler = null;
      fluidHandler = null;
      fluidTank = null;
    }
  }


  /* Fuel updating */

  /**
   * Tries to consume fuel from the given fluid handler
   * @param handler  Handler to consume fuel from
   * @return   Temperature of the consumed fuel, 0 if none found
   */
  private int trySolidFuel(IItemHandler handler, boolean consume) {
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      int time = ForgeHooks.getBurnTime(stack, TinkerRecipeTypes.FUEL.get()) / 4;
      if (time > 0) {
        MeltingFuel solid = MeltingFuelLookup.getSolid();
        if (consume) {
          ItemStack extracted = handler.extractItem(i, 1, false);
          if (ItemStack.isSameItem(extracted, stack)) {
            fuel += time;
            fuelQuality = time;
            temperature = solid.getTemperature();
            rate = solid.getRate();
            parent.setChangedFast();
            // return the container
            ItemStackTemplate remainderTemplate = extracted.getItem().getCraftingRemainder(extracted);
            ItemStack container = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
            if (!container.isEmpty()) {
              // if we cannot insert the container back, spit it on the ground
              ItemStack notInserted = ItemHandlerHelper.insertItem(handler, container, false);
              if (!notInserted.isEmpty()) {
                Level world = getLevel();
                double x = (world.getRandom().nextFloat() * 0.5F) + 0.25D;
                double y = (world.getRandom().nextFloat() * 0.5F) + 0.25D;
                double z = (world.getRandom().nextFloat() * 0.5F) + 0.25D;
                ItemEntity itementity = new ItemEntity(world, fuelPos.getX() + x, fuelPos.getY() + y, fuelPos.getZ() + z, container);
                itementity.setDefaultPickUpDelay();
                world.addFreshEntity(itementity);
              }
            }
          } else {
            TConstruct.LOG.error("Invalid item removed from solid fuel handler");
          }
        }
        return solid.getTemperature();
      }
    }
    return 0;
  }

  /** Fetches any relevant fuel handlers from the target position */
  private void fetchHandlers() {
    // if we have handlers, nothing to do
    if (fluidHandler != null && itemHandler != null) {
      return;
    }
    BlockEntity te = getLevel().getBlockEntity(fuelPos);
    if (te != null) {
      // first, identify a capability that has what we need
      // on the chance both are present, we prioritize fluid; we don't expect that to change
      ResourceHandler<FluidResource> fluidRh = getLevel().getCapability(Capabilities.Fluid.BLOCK, fuelPos, null, te, null);
      fluidTank = fluidRh;
      IFluidHandler fluidCapability = fluidRh == null ? null : IFluidHandler.of(fluidRh);
      fluidHandler = LazyOptional.ofNullable(fluidCapability);
      if (fluidHandler.isPresent()) {
        fluidHandler.addListener(fluidListener);
      }
      ResourceHandler<ItemResource> itemRh = getLevel().getCapability(Capabilities.Item.BLOCK, fuelPos, null, te, null);
      IItemHandler itemCapability = itemRh == null ? null : IItemHandler.of(itemRh);
      itemHandler = LazyOptional.ofNullable(itemCapability);
      if (itemHandler.isPresent()) {
        itemHandler.addListener(itemListener);
      }
    } else {
      fluidTank = null;
      fluidHandler = LazyOptional.empty();
      itemHandler = LazyOptional.empty();
    }
  }

  @Override
  public int findFuel(boolean consume) {
    fetchHandlers();
    assert fluidHandler != null;
    assert itemHandler != null;

    // prioritize liquid fuel - it usually goes hotter
    int temperature = 0;
    if (fluidHandler.isPresent()) {
      temperature = tryLiquidFuel(fluidHandler.orElse(EmptyFluidHandler.INSTANCE), consume);
    }
    // next, try solid fuel
    if (temperature == 0 && itemHandler.isPresent()) {
      temperature = trySolidFuel(itemHandler.orElse(EmptyItemHandler.INSTANCE), consume);
    }
    // no handler found, tell client of the lack of fuel
    if (temperature == 0 && consume) {
      this.temperature = 0;
      this.rate = 0;
    }
    return temperature;
  }


  /* UI Syncing */

  @Override
  public FuelInfo getFuelInfo() {
    fetchHandlers();
    assert itemHandler != null;

    FuelInfo info = super.getFuelInfo();
    if (info.isEmpty() && itemHandler.isPresent()) {
      return FuelInfo.ITEM;
    }
    return info;
  }


  /* Fluid handler */

  /** Gets the fluid handler for proxy */
  public ResourceHandler<FluidResource> getTank() {
    return fluidTank != null ? fluidTank : EmptyResourceHandler.instance();
  }
}
