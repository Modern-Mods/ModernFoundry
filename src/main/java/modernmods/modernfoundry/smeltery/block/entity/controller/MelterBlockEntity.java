package modernmods.modernfoundry.smeltery.block.entity.controller;
import modernmods.modernfoundry.smeltery.block.entity.ILegacyCapabilityBlockEntity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.modernfoundry.compat.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.block.entity.NameableBlockEntity;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.model.ModelProperties;
import modernmods.modernfoundry.library.fluid.FluidTankAnimated;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.utils.NBTTags;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.controller.ControllerBlock;
import modernmods.modernfoundry.smeltery.block.controller.MelterBlock;
import modernmods.modernfoundry.smeltery.block.entity.ITankBlockEntity.ITankInventoryBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.module.MeltingModuleInventory;
import modernmods.modernfoundry.smeltery.block.entity.module.SolidFuelModule;
import modernmods.modernfoundry.smeltery.menu.MelterContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MelterBlockEntity extends NameableBlockEntity implements ITankInventoryBlockEntity, ILegacyCapabilityBlockEntity {

  /** Max capacity for the tank */
  private static final int TANK_CAPACITY = FluidValues.INGOT * 24;
  /* tags */
  private static final String TAG_INVENTORY = "inventory";
  /** Name of the GUI */
  private static final MutableComponent NAME = TConstruct.makeTranslation("gui", "melter");

  public static final BlockEntityTicker<MelterBlockEntity> SERVER_TICKER = (level, pos, state, self) -> self.tick(level, pos, state);

  /* Tank */
  /** Internal fluid tank output */
  @Getter
  protected final FluidTankAnimated tank = new FluidTankAnimated(TANK_CAPACITY, this);
  /** Capability holder for the tank */
  private final LazyOptional<IFluidHandler> tankHolder = LazyOptional.of(() -> tank);
  /** Last comparator strength to reduce block updates */
  @Getter @Setter
  private int lastStrength = -1;

  /** Internal tick counter */
  private int tick;

  /* Heating */
  /** Handles all the melting needs */
  private final MeltingModuleInventory meltingInventory = new MeltingModuleInventory(this, tank, Config.COMMON.melterOreRate, 3);
  /** Capability holder for the tank */
  private final LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> meltingInventory);

  /** Fuel handling logic */
  @Getter
  private final SolidFuelModule fuelModule;

  /** Main constructor */
  public MelterBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.melter.get(), pos, state);
  }

  /** Extendable constructor */
  @SuppressWarnings("WeakerAccess")
  protected MelterBlockEntity(BlockEntityType<? extends MelterBlockEntity> type, BlockPos pos, BlockState state) {
    super(type, pos, state, NAME);
    this.fuelModule = new SolidFuelModule(this, pos.below());
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player playerEntity) {
    return new MelterContainerMenu(id, inv, this);
  }

  @Override
  public MeltingModuleInventory getItemHandler() {
    return meltingInventory;
  }

  /*
   * Tank methods
   */

  @Override
  public @NotNull ModelData getModelData() {
    return ModelData.builder()
                    .with(ModelProperties.FLUID_STACK, tank.getFluid())
                    .with(ModelProperties.TANK_CAPACITY, tank.getCapacity()).build();
  }

  @Nonnull
  public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
    if (capability == ForgeCapabilities.FLUID_HANDLER) {
      return tankHolder.cast();
    }
    if (capability == ForgeCapabilities.ITEM_HANDLER) {
      return inventoryHolder.cast();
    }
    return modernmods.modernfoundry.compat.neoforged.neoforge.common.util.LazyOptional.empty(); // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent
  }

  public void invalidateCaps() {
    // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent (was super.invalidateCaps();)
    this.tankHolder.invalidate();
    this.inventoryHolder.invalidate();
  }

  /*
   * Melting
   */

  /** Checks if the tile entity is active */
  private boolean isFormed() {
    BlockState state = this.getBlockState();
    return state.hasProperty(MelterBlock.IN_STRUCTURE) && state.getValue(MelterBlock.IN_STRUCTURE);
  }

  /** Ticks the TE on the server */
  private void tick(Level level, BlockPos pos, BlockState state) {
    // are we fully formed?
    if (isFormed()) {
      switch (tick) {
        // tick 0: find fuel
        case 0 -> {
          if (!fuelModule.hasFuel() && meltingInventory.canHeat(fuelModule.findFuel(false))) {
            fuelModule.findFuel(true);
          }
        }
        // tick 2: heat items and consume fuel
        case 2 -> {
          boolean hasFuel = fuelModule.hasFuel();
          // update the active state
          if (state.getValue(ControllerBlock.ACTIVE) != hasFuel) {
            level.setBlockAndUpdate(pos, state.setValue(ControllerBlock.ACTIVE, hasFuel));
            // update the heater below
            BlockPos down = pos.below();
            BlockState downState = level.getBlockState(down);
            if (downState.is(TinkerTags.Blocks.FUEL_TANKS) && downState.hasProperty(ControllerBlock.ACTIVE) && downState.getValue(ControllerBlock.ACTIVE) != hasFuel) {
              level.setBlockAndUpdate(down, downState.setValue(ControllerBlock.ACTIVE, hasFuel));
            }
          }
          // heat items
          if (hasFuel) {
            meltingInventory.heatItems(fuelModule.getTemperature(), fuelModule.getRate());
            fuelModule.decreaseFuel(1);
          } else {
            meltingInventory.coolItems();
          }
        }
      }
    } else if (tick == 2) {
      // if we have fuel, lose fuel
      if (fuelModule.hasFuel()) {
        fuelModule.decreaseFuel(1);
      } else {
        // if we lack fuel, cool items
        meltingInventory.coolItems();
      }
    }
    tick = (tick + 1) % 4;
  }


  /*
   * NBT
   */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  private HolderLookup.Provider getRegistryAccess() {
    return level == null ? TagUtil.BUILTIN_LOOKUP : level.registryAccess();
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    tank.readFromNBT(TagUtil.BUILTIN_LOOKUP, tag.getCompound(NBTTags.TANK));
    fuelModule.readFromTag(tag);
    if (tag.contains(TAG_INVENTORY, Tag.TAG_COMPOUND)) {
      meltingInventory.readFromTag(tag.getCompound(TAG_INVENTORY), getRegistryAccess());
    }
  }

  @Override
  public void saveSynced(CompoundTag tag) {
    super.saveSynced(tag);
    tag.put(NBTTags.TANK, tank.writeToNBT(TagUtil.BUILTIN_LOOKUP, new CompoundTag()));
    tag.put(TAG_INVENTORY, meltingInventory.writeToTag(getRegistryAccess()));
  }

  @Override
  public void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    fuelModule.writeToTag(tag);
  }
}
