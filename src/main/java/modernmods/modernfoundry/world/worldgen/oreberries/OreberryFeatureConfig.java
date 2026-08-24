package modernmods.modernfoundry.world.worldgen.oreberries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

/** Datapack configuration for one canonical Oreberries bush cluster. */
public record OreberryFeatureConfig(BlockState state, int sizeChance, List<ResourceLocation> replaceBlocks) implements FeatureConfiguration {
  public static final Codec<OreberryFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    BlockState.CODEC.fieldOf("state").forGetter(OreberryFeatureConfig::state),
    Codec.intRange(1, 256).fieldOf("size_chance").forGetter(OreberryFeatureConfig::sizeChance),
    ResourceLocation.CODEC.listOf().fieldOf("replace_blocks").forGetter(OreberryFeatureConfig::replaceBlocks)
  ).apply(instance, OreberryFeatureConfig::new));
}
