package modernmods.modernfoundry.smeltery.menu;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import modernmods.mantle.fluid.FluidTransferHelper;
import modernmods.mantle.fluid.transfer.IFluidContainerTransfer.TransferDirection;
import modernmods.mantle.fluid.transfer.IFluidContainerTransfer.TransferResult;
import modernmods.mantle.inventory.SmartItemHandlerSlot;
import modernmods.mantle.util.sync.ValidZeroDataSlot;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.shared.inventory.TriggeringBaseContainerMenu;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.entity.controller.MelterBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.module.MeltingModuleInventory;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MelterContainerMenu extends TriggeringBaseContainerMenu<MelterBlockEntity> {
  public static final Identifier TOOLTIP_FORMAT = TConstruct.getResource("melter");

  @SuppressWarnings("MismatchedReadAndWriteOfArray")
  @Getter
  private final Slot[] inputs;
  @Getter
  private boolean hasFuelSlot = false;
  public MelterContainerMenu(int id, @Nullable Inventory inv, @Nullable MelterBlockEntity melter) {
    super(TinkerSmeltery.melterContainer.get(), id, inv, melter);

    // create slots
    if (melter != null) {
      MeltingModuleInventory inventory = melter.getItemHandler();
      inputs = new Slot[inventory.getSlots()];
      for (int i = 0; i < inputs.length; i++) {
        inputs[i] = this.addSlot(new SmartItemHandlerSlot(inventory, i, 22, 16 + (i * 18)));
      }

      // add fuel slot if present, we only add for the melter though
      Level world = melter.getLevel();
      BlockPos down = melter.getBlockPos().below();
      if (world != null && world.getBlockState(down).is(TinkerTags.Blocks.FUEL_TANKS)) {
        BlockEntity te = world.getBlockEntity(down);
        if (te != null) {
          var handlerRh = world.getCapability(Capabilities.Item.BLOCK, down, world.getBlockState(down), te, null);
          IItemHandler handler = handlerRh == null ? null : IItemHandler.of(handlerRh);
          hasFuelSlot = handler != null;
          if (handler != null) {
            this.addSlot(new SmartItemHandlerSlot(handler, 0, 151, 32));
          }
        }
      }

      this.addInventorySlots();

      // syncing
      Consumer<DataSlot> referenceConsumer = this::addDataSlot;
      ValidZeroDataSlot.trackIntArray(referenceConsumer, melter.getFuelModule());
      inventory.trackInts(array -> ValidZeroDataSlot.trackIntArray(referenceConsumer, array));
    } else {
      inputs = new Slot[0];
    }
  }

  public MelterContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, MelterBlockEntity.class));
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (0 <= id && id <= 3 && !player.isSpectator()) {
      ItemStack held = getCarried();
      if (!held.isEmpty()) {
        if (!player.level().isClientSide() && tile != null) {
          ResourceHandler<FluidResource> tank = id < 2 ? tile.getTank() : tile.getFuelModule().getTank();
          TransferResult result;
          // even means drain fluid, odd means fill
          if ((id & 1) == 0) {
            FluidStack current = tank.size() > 0 ? tank.getResource(0).toStack(tank.getAmountAsInt(0)) : FluidStack.EMPTY;
            result = FluidTransferHelper.fillStack(tank, held, current);
          } else {
            result = FluidTransferHelper.interactWithStack(tank, held, TransferDirection.EMPTY_ITEM);
          }
          setCarried(FluidTransferHelper.handleUIResult(player, held, result));
        }
        return true;
      }
    }
    return false;
  }
}
