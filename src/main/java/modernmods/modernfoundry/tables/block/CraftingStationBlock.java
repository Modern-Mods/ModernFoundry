package modernmods.modernfoundry.tables.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.modernfoundry.tables.block.entity.table.CraftingStationBlockEntity;

import javax.annotation.Nullable;

public class CraftingStationBlock extends RetexturedTableBlock {

  public CraftingStationBlock(Properties builder) {
    super(builder);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new CraftingStationBlockEntity(pPos, pState);
  }
}
