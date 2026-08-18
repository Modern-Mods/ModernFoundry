package modernmods.modernfoundry.smeltery.block.component;

import net.minecraft.core.BlockPos;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import modernmods.mantle.block.RetexturedBlock;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.smeltery.block.entity.component.SmelteryComponentBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

public class RetexturedOrientableSmelteryBlock extends OrientableSmelteryBlock {
  public RetexturedOrientableSmelteryBlock(Properties properties, BlockEntitySupplier<? extends SmelteryComponentBlockEntity> blockEntity) {
    super(properties, true, blockEntity);
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
