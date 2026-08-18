package modernmods.modernfoundry.smeltery.block.controller;

import net.minecraft.core.BlockPos;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import modernmods.mantle.block.RetexturedBlock;
import modernmods.mantle.inventory.BaseContainerMenu;
import modernmods.mantle.util.BlockEntityHelper;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.multiblock.MultiblockResult;
import modernmods.modernfoundry.smeltery.network.StructureErrorPositionPacket;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Multiblock that displays the error from the tile entity on right click
 */
public abstract class HeatingControllerBlock extends ControllerBlock {
  protected HeatingControllerBlock(Properties builder) {
    super(builder);
  }

  @Override
  protected boolean openGui(Player player, Level world, BlockPos pos) {
    BlockState state = world.getBlockState(pos);
    boolean opened = false;
    if (state.getBlock() == this) {
      if (canOpenGui(state)) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
          MenuProvider container = this.getMenuProvider(state, world, pos);
          if (container != null) {
            serverPlayer.openMenu(container, buffer -> {
              buffer.writeBlockPos(pos);
              BlockEntityHelper.get(HeatingStructureBlockEntity.class, world, pos)
                .ifPresent(te -> buffer.writeVarInt(te.getMeltingInventory().getSlots()));
            });
            if (player.containerMenu instanceof BaseContainerMenu<?> menu) {
              menu.syncOnOpen(serverPlayer);
            }
          }
        }
        opened = true;
      } else {
        opened = displayStatus(player, world, pos, state);
      }
    }

    // only need to update if holding the proper items
    if (!world.isClientSide()) {
      BlockEntityHelper.get(HeatingStructureBlockEntity.class, world, pos).ifPresent(te -> {
        MultiblockResult result = te.getStructureResult();
        if (!result.isSuccess() && te.showDebugBlockBorder(player)) {
          TinkerNetwork.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), player);
        }
      });
    }
    return opened;
  }

  @Override
  protected boolean displayStatus(Player player, Level world, BlockPos pos, BlockState state) {
    if (!world.isClientSide()) {
      BlockEntityHelper.get(HeatingStructureBlockEntity.class, world, pos).ifPresent(te -> {
        MultiblockResult result = te.getStructureResult();
        if (!result.isSuccess()) {
          player.sendOverlayMessage(result.getMessage());
          TinkerNetwork.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), player);
        }
      });
    }
    return true;
  }

  // 26.1.2 removed block-level appendHoverText; the retextured tooltip is now shown by RetexturedBlockItem at item level.

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    RetexturedBlock.updateTextureBlock(world, pos, stack);
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
    return RetexturedBlock.getPickBlock(world, pos, state);
  }
}
