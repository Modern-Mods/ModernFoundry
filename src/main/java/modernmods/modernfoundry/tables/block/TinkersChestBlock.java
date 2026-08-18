package modernmods.modernfoundry.tables.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import modernmods.modernfoundry.compat.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import modernmods.mantle.util.BlockEntityHelper;
import modernmods.modernfoundry.tables.block.entity.chest.TinkersChestBlockEntity;

public class TinkersChestBlock extends ChestBlock {
  public TinkersChestBlock(Properties builder, BlockEntitySupplier<? extends BlockEntity> be, boolean dropsItems) {
    super(builder, be, dropsItems);
  }

  @Override
  protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
    ItemStack stack = new ItemStack(this);
    BlockEntityHelper.get(TinkersChestBlockEntity.class, world, pos).ifPresent(te -> {
      if (te.hasColor()) {
        ((DyeableLeatherItem) stack.getItem()).setColor(stack, te.getColor());
      }
    });
    return stack;
  }
}
