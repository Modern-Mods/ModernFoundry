package modernmods.modernfoundry.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.NetherFungusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.world.TinkerWorld;

/** Update of fungus that grows on slime soil instead */
// block codec identity is inherited from NetherFungusBlock (only relevant to datapack/worldgen serialization)
public class SlimeFungusBlock extends NetherFungusBlock {
  public SlimeFungusBlock(Properties properties, ResourceKey<ConfiguredFeature<?,?>> fungusFeature) {
    // NetherFungusBlock now takes the support-block tag; SLIMY_SOIL matches the mayPlaceOn override below
    super(fungusFeature, TinkerWorld.slimeDirt.get(DirtType.ICHOR), TinkerTags.Blocks.SLIMY_SOIL, properties);
  }

  @Override
  protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
    return state.is(TinkerTags.Blocks.SLIMY_SOIL);
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState state) {
    return worldIn.getBlockState(pos.below()).is(TinkerTags.Blocks.SLIMY_SOIL);
  }
}
