package modernmods.modernfoundry.smeltery.block.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.mantle.util.BlockEntityHelper;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.entity.controller.AlloyerBlockEntity;

import javax.annotation.Nullable;

public class AlloyerBlock extends TinyMultiblockControllerBlock {
  public AlloyerBlock(Properties builder) {
    super(builder);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new AlloyerBlockEntity(pPos, pState);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> wanted) {
    return pLevel.isClientSide() ? null : BlockEntityHelper.castTicker(wanted, TinkerSmeltery.alloyer.get(), AlloyerBlockEntity.SERVER_TICKER);
  }

  @Override
  public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, @org.jetbrains.annotations.Nullable net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
    // 26.1.2 replaced fromPos with an Orientation; getFront() is the direction the update came from
    if (orientation != null) {
      Direction direction = orientation.getFront();
      if (direction != Direction.DOWN) {
        BlockEntityHelper.get(AlloyerBlockEntity.class, world, pos).ifPresent(te -> te.neighborChanged(direction));
      }
    }
  }

  /*
   * Display
   */

  @Deprecated
  @Override
  public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
    return 1.0F;
  }

  @Override
  protected boolean propagatesSkylightDown(BlockState state) {
    return true;
  }

  @Override
  public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
    if (state.getValue(ACTIVE)) {
      double x = pos.getX() + 0.5D;
      double y = (double) pos.getY() + (rand.nextFloat() * 4F) / 16F;
      double z = pos.getZ() + 0.5D;
      double frontOffset = 0.52D;
      double sideOffset = rand.nextDouble() * 0.6D - 0.3D;
      spawnFireParticles(world, state, x, y, z, frontOffset, sideOffset, ParticleTypes.SOUL_FIRE_FLAME);
    }
  }
}
