package modernmods.modernfoundry.smeltery.block.entity.component;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.model.ModelProperties;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.entity.component.SmelteryInputOutputBlockEntity.SmelteryFluidIO;
import modernmods.modernfoundry.smeltery.block.entity.inventory.DuctItemHandler;
import modernmods.modernfoundry.smeltery.block.entity.inventory.DuctTankWrapper;
import modernmods.modernfoundry.smeltery.menu.SingleItemContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Filtered drain tile entity
 */
public class DuctBlockEntity extends SmelteryFluidIO implements MenuProvider {
  private static final String TAG_ITEM = "item";
  private static final Component TITLE = TConstruct.makeTranslation("gui", "duct");

  @Getter
  private final DuctItemHandler itemHandler = new DuctItemHandler(this);
  private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> itemHandler);

  public DuctBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.duct.get(), pos, state);
  }

  protected DuctBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }


  /* Container */

  @Override
  public Component getDisplayName() {
    return TITLE;
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player playerEntity) {
    return new SingleItemContainerMenu(id, inventory, this);
  }


  /* Capability */

  @Nonnull
  @Override
  public <C> LazyOptional<C> getCapability(Capability<C> capability, @Nullable Direction facing) {
    if (capability == ForgeCapabilities.ITEM_HANDLER) {
      return itemCapability.cast();
    }
    return super.getCapability(capability, facing);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    itemCapability.invalidate();
  }

  @Override
  protected LazyOptional<IFluidHandler> makeWrapper(LazyOptional<IFluidHandler> capability) {
    return LazyOptional.of(() -> new DuctTankWrapper(capability.orElse(emptyInstance), itemHandler));
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    return RetexturedHelper.getModelDataBuilder(getTexture()).with(ModelProperties.FLUID_STACK, itemHandler.getFluid().copy()).build();
  }

  /** Updates the fluid in model data */
  public void updateFluid() {
    requestModelDataUpdate();
    assert level != null;
    BlockState state = getBlockState();
    level.sendBlockUpdated(worldPosition, state, state, 48);
  }


  /* NBT */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    input.read(TAG_ITEM, CompoundTag.CODEC).ifPresent(itemHandler::readFromNBT);
  }

  @Override
  public void handleUpdateTag(ValueInput input) {
    super.handleUpdateTag(input);
    if (level != null && level.isClientSide()) {
      updateFluid();
    }
  }

  @Override
  public void saveSynced(ValueOutput output) {
    super.saveSynced(output);
    output.store(TAG_ITEM, CompoundTag.CODEC, itemHandler.writeToNBT());
  }
}
