package modernmods.modernfoundry.smeltery.block.component;

import net.minecraft.world.level.block.state.BlockState;

public class SearedTintedGlassBlock extends SearedGlassBlock {
  public SearedTintedGlassBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected boolean propagatesSkylightDown(BlockState state) {
    return false;
  }

  @Override
  protected int getLightDampening(BlockState state) {
    return 15;
  }
}
