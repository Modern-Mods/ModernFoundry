package modernmods.modernfoundry.smeltery.block.entity.component;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.library.client.model.ModelProperties;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.entity.component.SmelteryInputOutputBlockEntity.SmelteryFluidIO;
import modernmods.modernfoundry.smeltery.block.entity.tank.IDisplayFluidListener;

import javax.annotation.Nonnull;

/**
 * Fluid IO extension to display controller fluid
 */
public class DrainBlockEntity extends SmelteryFluidIO implements IDisplayFluidListener {
  @Getter
  private FluidStack displayFluid = FluidStack.EMPTY;

  public DrainBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.drain.get(), pos, state);
  }

  protected DrainBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    return RetexturedHelper.getModelDataBuilder(getTexture()).with(ModelProperties.FLUID_STACK, displayFluid).build();
  }

  @Override
  public void notifyDisplayFluidUpdated(FluidStack fluid) {
    if (!FluidStack.isSameFluidSameComponents(fluid, displayFluid)) {
      // no need to copy as the fluid was copied by the caller
      displayFluid = fluid;
      requestModelDataUpdate();
      assert level != null;
      BlockState state = getBlockState();
      level.sendBlockUpdated(worldPosition, state, state, 48);
    }
  }


  /* Updating */

  // override instead of saveSynced to avoid writing master to the main tag twice
  @Override
  public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
    CompoundTag nbt = super.getUpdateTag(registries);
    net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries);
    writeMaster(output);
    nbt.merge(output.buildResult());
    return nbt;
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }
}
