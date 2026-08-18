package modernmods.modernfoundry.smeltery.block.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.mantle.compat.neoforged.neoforge.capabilities.Capability;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import modernmods.mantle.block.entity.NameableBlockEntity;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.entity.inventory.HeaterItemHandler;
import modernmods.modernfoundry.smeltery.menu.SingleItemContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Tile entity for the heater block below the melter */
public class HeaterBlockEntity extends NameableBlockEntity implements ILegacyCapabilityBlockEntity {
  private static final String TAG_ITEM = "item";
  private static final Component TITLE = TConstruct.makeTranslation("gui", "heater");

  private final HeaterItemHandler itemHandler = new HeaterItemHandler(this);
  private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> itemHandler);

  protected HeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state, TITLE);
  }

  public HeaterBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.heater.get(), pos, state);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player playerEntity) {
    return new SingleItemContainerMenu(id, inventory, this);
  }


  /* Capability */

  @Nonnull
  public <C> LazyOptional<C> getCapability(Capability<C> capability, @Nullable Direction facing) {
    if (capability == ForgeCapabilities.ITEM_HANDLER) {
      return itemCapability.cast();
    }
    return modernmods.mantle.compat.neoforged.neoforge.common.util.LazyOptional.empty(); // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent
  }

  public void invalidateCaps() {
    // TODO(neoforge-capabilities): re-expose via RegisterCapabilitiesEvent (was super.invalidateCaps();)
    itemCapability.invalidate();
  }


  /* NBT */

  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    input.read(TAG_ITEM, CompoundTag.CODEC).ifPresent(itemHandler::readFromNBT);
  }

  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.store(TAG_ITEM, CompoundTag.CODEC, itemHandler.writeToNBT());
  }
}
