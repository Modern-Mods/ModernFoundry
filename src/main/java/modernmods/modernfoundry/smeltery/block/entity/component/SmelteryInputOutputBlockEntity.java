package modernmods.modernfoundry.smeltery.block.entity.component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import modernmods.modernfoundry.smeltery.block.entity.ILegacyCapabilityBlockEntity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import java.util.function.Consumer;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.mantle.block.entity.IRetexturedBlockEntity;
import modernmods.mantle.inventory.EmptyItemHandler;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.mantle.util.WeakConsumerWrapper;
import modernmods.modernfoundry.common.multiblock.IMasterLogic;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.entity.tank.ISmelteryTankHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static modernmods.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Shared logic between drains and ducts
 */
public abstract class SmelteryInputOutputBlockEntity<T> extends SmelteryComponentBlockEntity implements IRetexturedBlockEntity, ILegacyCapabilityBlockEntity {
  /** Capability this TE watches */
  private final Capability<T> capability;
  /** Empty capability for in case the valid capability becomes invalid without invalidating */
  protected final T emptyInstance;
  /** Listener to attach to consumed capabilities */
  protected final Consumer<LazyOptional<T>> listener = new WeakConsumerWrapper<>(this, (te, cap) -> te.clearHandler());
  @Nullable
  private LazyOptional<T> capabilityHolder = null;

  /* Retexturing */
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;

  protected SmelteryInputOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Capability<T> capability, T emptyInstance) {
    super(type, pos, state);
    this.capability = capability;
    this.emptyInstance = emptyInstance;
  }

  /** Clears all cached capabilities */
  private void clearHandler() {
    if (capabilityHolder != null) {
      capabilityHolder.invalidate();
      capabilityHolder = null;
    }
  }

  public void invalidateCaps() {
    // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent (was super.invalidateCaps();)
    clearHandler();
  }

  @Override
  public void onMasterLoad(IMasterLogic master) {
    clearHandler();
  }

  @Override
  protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
    assert level != null;

    // if we have a new master, invalidate handlers
    boolean masterChanged = false;
    if (!Objects.equals(getMasterPos(), master)) {
      clearHandler();
      masterChanged = true;
    }
    super.setMaster(master, block);
    // notify neighbors of the change (state change skips the notify flag)
    if (masterChanged) {
      level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }
  }

  /**
   * Gets the capability to store in this IO block. Capability parent should have the proper listeners attached
   * @param parent  Parent tile entity
   * @return  Capability from parent, or empty if absent
   */
  protected LazyOptional<T> getCapability(BlockEntity parent) {
    LazyOptional<T> handler = parent instanceof ILegacyCapabilityBlockEntity provider ? provider.getCapability(capability, null) : LazyOptional.empty();
    if (handler.isPresent()) {
      handler.addListener(listener);

      return LazyOptional.of(() -> handler.orElse(emptyInstance));
    }
    return LazyOptional.empty();
  }

  /**
   * Fetches the capability handlers if missing
   */
  private LazyOptional<T> getCachedCapability() {
    if (capabilityHolder == null) {
      if (validateMaster()) {
        BlockPos master = getMasterPos();
        if (master != null && this.level != null) {
          BlockEntity te = level.getBlockEntity(master);
          if (te != null) {
            capabilityHolder = getCapability(te);
            return capabilityHolder;
          }
        }
      }
      capabilityHolder = LazyOptional.empty();
    }
    return capabilityHolder;
  }

  @Nonnull
  public <C> LazyOptional<C> getCapability(Capability<C> capability, @Nullable Direction facing) {
    if (capability == this.capability) {
      return getCachedCapability().cast();
    }
    return modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional.empty(); // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent
  }


  /* Retexturing */

  @Override
  @Nonnull
  public ModelData getModelData() {
    return RetexturedHelper.getModelData(getTexture());
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = texture;
    texture = RetexturedHelper.getBlock(name);
    if (oldTexture != texture) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }


  /* NBT */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(ValueOutput output) {
    super.saveSynced(output);
    if (texture != Blocks.AIR) {
      output.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    String tex = input.getStringOr(TAG_TEXTURE, "");
    if (!tex.isEmpty()) {
      texture = RetexturedHelper.getBlock(tex);
      RetexturedHelper.onTextureUpdated(this);
    }
  }


  /** Fluid implementation of smeltery IO */
  public static abstract class SmelteryFluidIO extends SmelteryInputOutputBlockEntity<IFluidHandler> {
    protected SmelteryFluidIO(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, ForgeCapabilities.FLUID_HANDLER, EmptyFluidHandler.INSTANCE);
    }

    /** Wraps the given capability */
    protected LazyOptional<IFluidHandler> makeWrapper(LazyOptional<IFluidHandler> capability) {
      return LazyOptional.of(() -> capability.orElse(emptyInstance));
    }

    @Override
    protected LazyOptional<IFluidHandler> getCapability(BlockEntity parent) {
      // fluid capability is not exposed directly in the smeltery
      if (parent instanceof ISmelteryTankHandler tankHandler) {
        LazyOptional<IFluidHandler> capability = tankHandler.getFluidCapability();
        if (capability.isPresent()) {
          capability.addListener(listener);
          return makeWrapper(capability);
        }
      }
      return LazyOptional.empty();
    }
  }

  /** Item implementation of smeltery IO */
  public static class ChuteBlockEntity extends SmelteryInputOutputBlockEntity<IItemHandler> {
    public ChuteBlockEntity(BlockPos pos, BlockState state) {
      this(TinkerSmeltery.chute.get(), pos, state);
    }

    protected ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, ForgeCapabilities.ITEM_HANDLER, EmptyItemHandler.INSTANCE);
    }
  }

}
