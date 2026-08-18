package modernmods.modernfoundry.smeltery.block.entity.component;
import modernmods.modernfoundry.smeltery.block.entity.ILegacyCapabilityBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import modernmods.modernfoundry.common.multiblock.IMasterLogic;
import modernmods.modernfoundry.library.client.model.ModelProperties;
import modernmods.modernfoundry.library.fluid.FluidTankAnimated;
import modernmods.modernfoundry.library.utils.NBTTags;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.component.SearedTankBlock;
import modernmods.modernfoundry.smeltery.block.component.SearedTankBlock.TankType;
import modernmods.modernfoundry.smeltery.block.entity.ITankBlockEntity;
import modernmods.modernfoundry.smeltery.item.TankItem;
import modernmods.modernfoundry.smeltery.network.FluidUpdatePacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TankBlockEntity extends SmelteryComponentBlockEntity implements ITankBlockEntity, ILegacyCapabilityBlockEntity, FluidUpdatePacket.IFluidPacketReceiver {
  /** Max capacity for the tank */
  public static final int DEFAULT_CAPACITY = FluidType.BUCKET_VOLUME * 4;

  /**
   * Gets the capacity for the given block
   * @param block  block
   * @return  Capacity
   */
  public static int getCapacity(Block block) {
    if (block instanceof ITankBlock) {
      return ((ITankBlock) block).getCapacity();
    }
    return DEFAULT_CAPACITY;
  }

  /**
   * Gets the capacity for the given item
   * @param item  item
   * @return  Capacity
   */
  public static int getCapacity(Item item) {
    if (item instanceof BlockItem) {
      return getCapacity(((BlockItem)item).getBlock());
    }
    return DEFAULT_CAPACITY;
  }

  /** Internal fluid tank instance */
  protected final FluidTankAnimated tank;
  /** Capability holder for the tank */
  private final LazyOptional<IFluidHandler> holder;
  /** Last comparator strength to reduce block updates */
  private int lastStrength = -1;

  public TankBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, state.getBlock() instanceof ITankBlock tank
                     ? tank
                     : TinkerSmeltery.searedTank.get(TankType.FUEL_TANK));
  }

  /** Main constructor */
  public TankBlockEntity(BlockPos pos, BlockState state, ITankBlock block) {
    this(TinkerSmeltery.tank.get(), pos, state, block);
  }

  /** Extendable constructor */
  @SuppressWarnings("WeakerAccess")
  protected TankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ITankBlock block) {
    super(type, pos, state);
    tank = new FluidTankAnimated(block.getCapacity(), this);
    holder = LazyOptional.of(() -> tank);
  }

  public FluidTankAnimated getTank() {
    return tank;
  }

  /**
   * Server tick for standalone tanks: pushes this tank's fluid into the tank directly below, so a vertical stack of
   * seared/scorched tanks fills from the bottom and drains from the top, behaving like one connected reservoir. Tanks that
   * are part of a smeltery/foundry (they have a master) are skipped — the structure manages their fluid.
   */
  public static void serverTick(Level level, BlockPos pos, BlockState state, TankBlockEntity tank) {
    // skip only tanks that are part of a VALID (formed) smeltery/foundry. hasMaster() is too broad: orphaned standalone
    // tanks keep a stale masterPos and report hasMaster()=true even though no real structure owns them. validateMaster()
    // re-checks the master block + IN_STRUCTURE and CLEARS a stale master, so those tanks fall through to the gravity flow.
    if (tank.validateMaster() || tank.tank.isEmpty()) {
      return;
    }
    if (level.getBlockEntity(pos.below()) instanceof TankBlockEntity below && !below.validateMaster()) {
      FluidStack fluid = tank.tank.getFluid();
      if (fluid.isEmpty()) {
        return;
      }
      // fill the lower tank with what fits, then move exactly that amount out of this one (fill-simulate then
      // execute both sides with the same amount -> no duplication)
      int fillable = below.tank.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
      if (fillable > 0) {
        FluidStack moved = tank.tank.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
        if (!moved.isEmpty()) {
          below.tank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        }
      }
    }
  }

  public int getLastStrength() {
    return lastStrength;
  }

  public void setLastStrength(int lastStrength) {
    this.lastStrength = lastStrength;
  }


  /*
   * Tank methods
   */

  @Nonnull
  public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
    if (capability == ForgeCapabilities.FLUID_HANDLER) {
      return holder.cast();
    }
    return modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional.empty(); // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent
  }

  public void invalidateCaps() {
    // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent (was super.invalidateCaps();)
    holder.invalidate();
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    return ModelData.builder()
                    .with(ModelProperties.FLUID_STACK, tank.getFluid())
                    .with(ModelProperties.TANK_CAPACITY, tank.getCapacity()).build();
  }

  /** Updates the light for this tank using {@link SearedTankBlock#LIGHT} */
  public static void updateLight(BlockEntity be, IFluidTank tank) {
    Level level = be.getLevel();
    if (level != null && !level.isClientSide()) {
      FluidStack fluid = tank.getFluid();
      int light = fluid.isEmpty() ? 0 : fluid.getFluid().getFluidType().getLightLevel(fluid);
      BlockState state = be.getBlockState();
      if (light != state.getValue(SearedTankBlock.LIGHT)) {
        level.setBlock(be.getBlockPos(), state.setValue(SearedTankBlock.LIGHT, light), Block.UPDATE_CLIENTS);
      }
    }
  }

  @Override
  public void onTankContentsChanged() {
    ITankBlockEntity.super.onTankContentsChanged();
    if (this.level != null) {
      updateLight(this, tank);
      this.requestModelDataUpdate();
    }
  }

  /**
   * Client-side handler for {@link FluidUpdatePacket}. Without this the tank only received its fluid via saved NBT on
   * chunk load, so a live fill/drain would not render until the world was reloaded. Applies the synced fluid and refreshes
   * the model data so the fluid appears immediately.
   */
  @Override
  public void updateFluidTo(FluidStack fluid) {
    // ITankBlockEntity's default applies the fluid AND seeds the render offset (the delta that drives the fill/drain
    // animation) plus refreshes the fluid-in-model data. We then fire onTankContentsChanged so the live sync renders
    // immediately without a world reload.
    ITankBlockEntity.super.updateFluidTo(fluid);
    onTankContentsChanged();
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide()) {
      BlockPos masterPos = getMasterPos();
      if (masterPos != null && level.getBlockEntity(masterPos) instanceof IMasterLogic master) {
        master.onServantLoad(this);
      }
    }
  }

  /*
   * NBT
   */

  /**
   * Sets the tag on the stack based on the contained tank
   * @param stack  Stack
   */
  public void setTankTag(ItemStack stack) {
    TankItem.setTank(stack, tank);
  }

  /**
   * Updates the tank from an NBT tag, used in the block
   * @param nbt  tank NBT
   */
  public void updateTank(CompoundTag nbt) {
    if (nbt.isEmpty()) {
      tank.setFluid(FluidStack.EMPTY);
    } else if (nbt.contains("FluidName")) {
      tank.setFluid(TankItem.readFluid(nbt));
      updateLight(this, tank);
    } else {
      tank.deserialize(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level == null ? HolderLookup.Provider.create(java.util.stream.Stream.empty()) : level.registryAccess(), nbt));
      updateLight(this, tank);
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
    tank.setCapacity(getCapacity(getBlockState().getBlock()));
    java.util.Optional<net.minecraft.world.level.storage.ValueInput> tankInput = input.child(NBTTags.TANK);
    if (tankInput.isPresent()) {
      tank.deserialize(tankInput.get());
      updateLight(this, tank);
    } else {
      tank.setFluid(FluidStack.EMPTY);
    }
    super.loadAdditional(input);
  }

  @Override
  public void saveSynced(net.minecraft.world.level.storage.ValueOutput output) {
    super.saveSynced(output);
    // want tank on the client on world load
    if (!tank.isEmpty()) {
      tank.serialize(output.child(NBTTags.TANK));
    }
  }

  /** Interface for blocks to return their capacity */
  public interface ITankBlock {
    /** Gets the capacity for this tank */
    int getCapacity();
  }
}
