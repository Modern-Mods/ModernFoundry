package modernmods.modernfoundry.shared.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import modernmods.mantle.block.IMultipartConnectedBlock;
import modernmods.mantle.client.model.connected.ConnectedModelRegistry;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ClearGlassPaneBlock extends BetterPaneBlock implements IMultipartConnectedBlock {
  public ClearGlassPaneBlock(Properties builder) {
    super(builder);
    this.registerDefaultState(IMultipartConnectedBlock.defaultConnections(this.defaultBlockState()));
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    IMultipartConnectedBlock.fillStateContainer(builder);
  }

  @Override
  protected BlockState updateShape(BlockState stateIn, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
    BlockState state = super.updateShape(stateIn, level, tickAccess, currentPos, facing, facingPos, facingState, random);
    return getConnectionUpdate(state, facing, facingState);
  }

  @Override
  public boolean connects(BlockState state, BlockState neighbor) {
    return ConnectedModelRegistry.getPredicate("pane").test(state, neighbor);
  }
}
