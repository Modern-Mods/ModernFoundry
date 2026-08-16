package modernmods.modernfoundry.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.hilt.block.GaugeBlock;
import modernmods.modernfoundry.smeltery.block.entity.GaugeBlockEntity;

public class RenderingGaugeBlock extends GaugeBlock implements EntityBlock {
  public RenderingGaugeBlock(Properties builder) {
    super(builder);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new GaugeBlockEntity(pos, state);
  }
}
