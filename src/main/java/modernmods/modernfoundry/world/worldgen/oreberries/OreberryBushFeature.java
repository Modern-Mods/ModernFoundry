package modernmods.modernfoundry.world.worldgen.oreberries;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.WorldGenLevel;

/** Native replacement for Oreberries' small/medium/tiny cluster generator. */
public class OreberryBushFeature extends Feature<OreberryFeatureConfig> {
  public OreberryBushFeature(Codec<OreberryFeatureConfig> codec) {
    super(codec);
  }

  @Override
  public boolean place(FeaturePlaceContext<OreberryFeatureConfig> context) {
    if (context.chunkGenerator() instanceof FlatLevelSource) {
      return false;
    }
    RandomSource random = context.random();
    BlockPos origin = context.origin();
    int type = random.nextInt(context.config().sizeChance());
    if (type == 11) {
      generateMedium(context.level(), random, origin, context.config());
    } else if (type >= 5) {
      generateSmall(context.level(), random, origin, context.config());
    } else {
      generateTiny(context.level(), random, origin, context.config());
    }
    return true;
  }

  private static void generateMedium(WorldGenLevel level, RandomSource random, BlockPos origin, OreberryFeatureConfig config) {
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          if (random.nextInt(4) == 0) {
            place(level, origin.offset(x, y, z), config);
          }
        }
      }
    }
    generateSmall(level, random, origin, config);
  }

  private static void generateSmall(WorldGenLevel level, RandomSource random, BlockPos origin, OreberryFeatureConfig config) {
    place(level, origin, config);
    if (random.nextBoolean()) place(level, origin.east(), config);
    if (random.nextBoolean()) place(level, origin.west(), config);
    if (random.nextBoolean()) place(level, origin.south(), config);
    if (random.nextBoolean()) place(level, origin.north(), config);
    if (random.nextInt(4) != 0) place(level, origin.above(), config);
  }

  private static void generateTiny(WorldGenLevel level, RandomSource random, BlockPos origin, OreberryFeatureConfig config) {
    place(level, origin, config);
    if (random.nextInt(4) == 0) place(level, origin.east(), config);
    if (random.nextInt(4) == 0) place(level, origin.west(), config);
    if (random.nextInt(4) == 0) place(level, origin.south(), config);
    if (random.nextInt(4) == 0) place(level, origin.north(), config);
    if (random.nextInt(16) < 7) place(level, origin.above(), config);
  }

  private static void place(WorldGenLevel level, BlockPos pos, OreberryFeatureConfig config) {
    BlockState existing = level.getBlockState(pos);
    boolean replaceable = existing.isAir() || !existing.isCollisionShapeFullBlock(level, pos)
      || config.replaceBlocks().stream().map(BuiltInRegistries.BLOCK::get).anyMatch(existing::is);
    if (replaceable && existing.getBlock() != Blocks.BEDROCK) {
      level.setBlock(pos, config.state(), 2);
    }
  }
}
