package modernmods.modernfoundry.smeltery.block.entity.module.alloying;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.mantle.fluid.FluidTransferHelper;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.recipe.alloying.IMutableAlloyTank;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Alloy tank that takes inputs from neighboring blocks
 */
@RequiredArgsConstructor
public class MixerAlloyTank implements IMutableAlloyTank {
  // parameters
  /** Handler parent */
  private final MantleBlockEntity parent;
  /** Tank for outputs */
  private final IFluidHandler outputTank;

  /** Current temperature. Provided as a getter and setter as there are a few contexts with different source for temperature */
  @Getter
  @Setter
  private int temperature = 0;

  // side tank cache
  /** Cache of tanks for each of the sides; a null value means the side was checked but holds no tank */
  private final Map<Direction,ResourceHandler<FluidResource>> inputs = new EnumMap<>(Direction.class);
  /** Map of tank index to tank on the side */
  @Nullable
  private List<ResourceHandler<FluidResource>> indexedList = null;

  // state
  /** If true, tanks are marked for refresh later */
  private boolean needsRefresh = true;
  /** Number of currently held tanks */
  private int currentTanks = 0;

  @Override
  public int getTanks() {
    checkTanks();
    return currentTanks;
  }

  /** Gets the indexed list of side tank handlers */
  private List<ResourceHandler<FluidResource>> indexTanks() {
    // convert map into indexed list of fluid handlers, will be cleared next time a side updates
    if (indexedList == null) {
      indexedList = new ArrayList<>(currentTanks);
      for (Direction direction : Direction.values()) {
        if (direction != Direction.DOWN) {
          ResourceHandler<FluidResource> handler = inputs.get(direction);
          if (handler != null) {
            indexedList.add(handler);
          }
        }
      }
    }
    return indexedList;
  }

  /** Gets the fluid handler for the given tank index */
  public ResourceHandler<FluidResource> getFluidHandler(int tank) {
    checkTanks();
    // invalid index, nothing
    if (tank >= currentTanks || tank < 0) {
      return EmptyResourceHandler.instance();
    }
    return indexTanks().get(tank);
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    checkTanks();
    // invalid index, nothing
    if (tank >= currentTanks || tank < 0) {
      return FluidStack.EMPTY;
    }
    // get the first fluid from the proper tank, we do not support multiple fluids on a side
    ResourceHandler<FluidResource> handler = indexTanks().get(tank);
    if (handler.size() > 0) {
      FluidResource resource = handler.getResource(0);
      return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(handler.getAmountAsInt(0));
    }
    return FluidStack.EMPTY;
  }

  @Override
  public FluidStack drain(int tank, FluidStack fluidStack) {
    checkTanks();
    // invalid index, nothing
    if (tank >= currentTanks || tank < 0) {
      return FluidStack.EMPTY;
    }
    return FluidTransferHelper.drain(indexTanks().get(tank), fluidStack, true);
  }

  @Override
  public boolean canFit(FluidStack fluid, int removed) {
    checkTanks();
    return outputTank.fill(fluid, FluidAction.SIMULATE) == fluid.getAmount();
  }

  @Override
  public int fill(FluidStack fluidStack) {
    return outputTank.fill(fluidStack, FluidAction.EXECUTE);
  }

  /**
   * Refreshes the cached tanks if needed
   * After calling this method, all five tank sides will have been fetched
   */
  private void checkTanks() {
    // need world to do anything
    Level world = parent.getLevel();
    if (world == null) {
      return;
    }
    if (needsRefresh) {
      for (Direction direction : Direction.values()) {
        // update each direction we are missing
        if (direction != Direction.DOWN && !inputs.containsKey(direction)) {
          BlockPos target = parent.getBlockPos().relative(direction);
          // limit by blocks as that gives the modpack more control, say they want to allow only scorched tanks
          if (world.getBlockState(target).is(TinkerTags.Blocks.ALLOYER_TANKS)) {
            BlockEntity te = world.getBlockEntity(target);
            if (te != null) {
              // if we found a tank, increment the number of tanks
              ResourceHandler<FluidResource> capability = world.getCapability(Capabilities.Fluid.BLOCK, target, null, te, direction.getOpposite());
              if (capability != null) {
                inputs.put(direction, capability);
                currentTanks++;
              } else {
                inputs.put(direction, null);
              }
            } else {
              inputs.put(direction, null);
            }
          }
        }
      }
      needsRefresh = false;
    }
  }

  /**
   * Called on block update or when a capability invalidates to mark that a direction needs updates
   * @param direction  Side updating
   * @param checkInput If true, validates that the side contains an input before reducing tank count. False when invalidated through the capability
   * */
  public void refresh(Direction direction, boolean checkInput) {
    if (direction == Direction.DOWN) {
      return;
    }
    if (!checkInput || (inputs.containsKey(direction) && inputs.get(direction) != null)) {
      currentTanks--;
    }
    inputs.remove(direction);
    needsRefresh = true;
    indexedList = null;
  }
}
