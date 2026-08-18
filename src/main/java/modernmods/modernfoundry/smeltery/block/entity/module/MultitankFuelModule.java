package modernmods.modernfoundry.smeltery.block.entity.module;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import modernmods.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * Fuel module that supports multiple tanks, selecting just one for the fuel result.
 * Exposes the aggregate of all fuel tanks as a {@link ResourceHandler} of {@link FluidResource} for the UI.
 */
public class MultitankFuelModule extends FuelModule implements ResourceHandler<FluidResource> {
  /** Block position that will never be valid in world, used for sync */
  private static final BlockPos NULL_POS = new BlockPos(0, Short.MIN_VALUE, 0);

  /** Supplier for the list of valid tank positions */
  private final Supplier<List<BlockPos>> tankSupplier;
  /** Position of the last fluid handler */
  private BlockPos lastPos = NULL_POS;

  /** Map of all tank handlers at each relevant position. Used for fast switching between handlers, notably in the UI */
  private Map<BlockPos,ResourceHandler<FluidResource>> tankHandlers;
  /** Combined view over all tank handlers, backing the aggregate resource handler behavior */
  @Nullable
  private ResourceHandler<FluidResource> combined;

  public MultitankFuelModule(MantleBlockEntity parent, Supplier<List<BlockPos>> tankSupplier) {
    super(parent);
    this.tankSupplier = tankSupplier;
  }

  /** Resets just the last fluid listener */
  private void clearLastListener() {
    super.resetHandler(null);
  }

  @Override
  protected void resetHandler(@Nullable LazyOptional<?> source) {
    if (source == null || source == fluidHandler) {
      this.lastPos = NULL_POS;
    }
    super.resetHandler(source);
  }

  /** Called on structure rebuild to clear the gui handler list */
  public void clearFluidListeners() {
    tankHandlers = null;
    combined = null;
  }

  /** Called on servant load to ensure the tank is present in the cache */
  public void ensureTankPresent(BlockEntity be) {
    BlockPos pos = be.getBlockPos();
    if (tankHandlers != null && !tankHandlers.containsKey(pos)) {
      ResourceHandler<FluidResource> rh = be.getLevel() == null ? null : be.getLevel().getCapability(Capabilities.Fluid.BLOCK, pos, null, be, null);
      if (rh != null) {
        tankHandlers.put(pos, rh);
        combined = null;
      }
    }
  }

  /** Gets the map from position to fluid handler */
  private Map<BlockPos,ResourceHandler<FluidResource>> getTankHandlers() {
    if (tankHandlers == null) {
      tankHandlers = new LinkedHashMap<>();
      Level world = getLevel();
      for (BlockPos pos : tankSupplier.get()) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null) {
          ResourceHandler<FluidResource> rh = world.getCapability(Capabilities.Fluid.BLOCK, pos, null, te, null);
          if (rh != null) {
            tankHandlers.put(pos, rh);
          }
        }
      }
      combined = null;
    }
    return tankHandlers;
  }

  /** Gets the combined resource handler over all fuel tanks, rebuilt when the tank list changes */
  private ResourceHandler<FluidResource> getCombined() {
    if (combined == null) {
      combined = new CombinedResourceHandler<>(new ArrayList<>(getTankHandlers().values()));
    }
    return combined;
  }


  /* Fuel finding */

  /**
   * Tries to consume fuel from the given position
   * @param pos  Position
   * @return   Temperature of the consumed fuel, 0 if none found
   */
  private int tryFuelPosition(BlockPos pos, boolean consume) {
    ResourceHandler<FluidResource> tankCap = getTankHandlers().get(pos);
    if (tankCap != null) {
      // if we find a valid cap, try to consume fuel from it (bridged to the legacy fuel logic)
      int temperature = tryLiquidFuel(IFluidHandler.of(tankCap), consume);
      if (temperature > 0) {
        clearLastListener();
        fluidHandler = LazyOptional.of(() -> IFluidHandler.of(tankCap));
        lastPos = pos;
        return temperature;
      }
    }
    return 0;
  }

  /**
   * Attempts to consume fuel from one of the tanks
   * @return  temperature of the found fluid, 0 if none
   */
  @Override
  public int findFuel(boolean consume) {
    // only fetch a handler if we haven't done so
    if (fluidHandler != null) {
      // if we have a handler, try to use that if possible
      if (fluidHandler.isPresent()) {
        int temperature = tryLiquidFuel(fluidHandler.orElse(EmptyFluidHandler.INSTANCE), consume);
        if (temperature > 0) {
          return temperature;
        }
      }
    } else if (lastPos != NULL_POS) {
      // if no handler, try to find one at the last position
      int posTemp = tryFuelPosition(lastPos, consume);
      if (posTemp > 0) {
        return posTemp;
      }
    }

    // find a new handler among our tanks
    for (BlockPos pos : tankSupplier.get()) {
      // already checked the last position above, no reason to try again
      if (!pos.equals(lastPos)) {
        int posTemp = tryFuelPosition(pos, consume);
        if (posTemp > 0) {
          return posTemp;
        }
      }
    }

    // no handler found, tell client of the lack of fuel
    if (consume) {
      temperature = 0;
      rate = 0;
    }
    return 0;
  }


  /* NBT */
  private static final String TAG_LAST_FUEL = "last_fuel";

  @Override
  public void readFromTag(CompoundTag nbt) {
    super.readFromTag(nbt);
    if (nbt.contains(TAG_LAST_FUEL)) {
      lastPos = modernmods.modernfoundry.library.utils.TagUtil.readBlockPos(nbt.getCompoundOrEmpty(TAG_LAST_FUEL), "pos").map(pos -> pos.offset(parent.getBlockPos())).orElse(NULL_POS);
    }
  }

  @Override
  public CompoundTag writeToTag(CompoundTag nbt) {
    nbt = super.writeToTag(nbt);
    if (lastPos != NULL_POS) {
      CompoundTag posTag = new CompoundTag();
      posTag.put("pos", modernmods.modernfoundry.library.utils.TagUtil.writeBlockPos(lastPos.subtract(parent.getBlockPos())));
      nbt.put(TAG_LAST_FUEL, posTag);
    }
    return nbt;
  }


  /* UI syncing */
  private static final int LAST_X = 4;
  private static final int LAST_Y = 5;
  private static final int LAST_Z = 6;

  @Override
  public int getCount() {
    return 7;
  }

  @Override
  public int get(int index) {
    return switch (index) {
      case LAST_X -> lastPos.getX();
      case LAST_Y -> lastPos.getY();
      case LAST_Z -> lastPos.getZ();
      default -> super.get(index);
    };
  }

  @Override
  public void set(int index, int value) {
    if (LAST_X <= index && index <= LAST_Z) {
      switch (index) {
        case LAST_X -> lastPos = new BlockPos(value, lastPos.getY(), lastPos.getZ());
        case LAST_Y -> lastPos = new BlockPos(lastPos.getX(), value, lastPos.getZ());
        case LAST_Z -> lastPos = new BlockPos(lastPos.getX(), lastPos.getY(), value);
      }
      clearLastListener();
    } else {
      super.set(index, value);
    }
  }

  @Override
  public FuelInfo getFuelInfo() {
    // if there is no position, means we have not yet consumed fuel. Just fetch the first tank
    // Y of big negative is how the UI syncs null
    BlockPos mainTank = lastPos;
    if (mainTank.getY() == NULL_POS.getY()) {
      // if no first, return no fuel info
      List<BlockPos> positions = tankSupplier.get();
      if (positions.isEmpty()) {
        return FuelInfo.EMPTY;
      }
      mainTank = positions.get(0);
      assert mainTank != null;
    }

    // fetch primary fuel handler
    if (fluidHandler == null) {
      ResourceHandler<FluidResource> fluidCap = getTankHandlers().get(mainTank);
      if (fluidCap != null) {
        fluidHandler = LazyOptional.of(() -> IFluidHandler.of(fluidCap));
      } else {
        // ensure handlers is set
        fluidHandler = LazyOptional.empty();
      }
    }

    // determine what fluid we have and how many other fluids we have
    FuelInfo info = super.getFuelInfo();
    // add extra fluid display
    if (!info.isEmpty()) {
      // add display info from each handler
      FluidStack currentFuel = info.getFluid();
      for (Entry<BlockPos,ResourceHandler<FluidResource>> entry : getTankHandlers().entrySet()) {
        if (!mainTank.equals(entry.getKey())) {
          ResourceHandler<FluidResource> handler = entry.getValue();
          if (handler.size() > 0) {
            FluidResource resource = handler.getResource(0);
            int capacity = handler.getCapacityAsInt(0, resource);
            FluidStack fluid = resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(handler.getAmountAsInt(0));
            // sum if empty (more capacity) or the same fluid (more amount and capacity)
            if (fluid.isEmpty()) {
              info.add(0, capacity);
            } else if (FluidStack.isSameFluidSameComponents(currentFuel, fluid)) {
              info.add(fluid.getAmount(), capacity);
            }
          }
        }
      }
    }

    return info;
  }


  /* Fluid handler */

  /** Gets the most recently used fluid */
  public FluidStack getLastFluid() {
    if (fluidHandler != null && fluidHandler.isPresent()) {
      return fluidHandler.orElse(EmptyFluidHandler.INSTANCE).getFluidInTank(0);
    }
    BlockPos pos;
    if (lastPos.getY() != NULL_POS.getY()) {
      pos = lastPos;
    } else {
      List<BlockPos> positions = tankSupplier.get();
      if (!positions.isEmpty()) {
        pos = positions.get(0);
      } else {
        return FluidStack.EMPTY;
      }
    }
    ResourceHandler<FluidResource> handler = getTankHandlers().get(pos);
    if (handler != null && handler.size() > 0) {
      FluidResource resource = handler.getResource(0);
      return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(handler.getAmountAsInt(0));
    }
    return FluidStack.EMPTY;
  }


  /* Aggregate resource handler over all fuel tanks */

  @Override
  public int size() {
    return getCombined().size();
  }

  @Override
  public FluidResource getResource(int index) {
    return getCombined().getResource(index);
  }

  @Override
  public long getAmountAsLong(int index) {
    return getCombined().getAmountAsLong(index);
  }

  @Override
  public long getCapacityAsLong(int index, FluidResource resource) {
    return getCombined().getCapacityAsLong(index, resource);
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    return getCombined().isValid(index, resource);
  }

  @Override
  public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
    return getCombined().insert(index, resource, amount, transaction);
  }

  @Override
  public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
    return getCombined().extract(index, resource, amount, transaction);
  }
}
