package modernmods.modernfoundry.world.worldgen.trees;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import modernmods.modernfoundry.world.TinkerStructures;
import modernmods.modernfoundry.world.block.FoliageType;

import java.util.Optional;

public final class SlimeTree {
  private SlimeTree() {}

  public static TreeGrower create(FoliageType foliageType) {
    return switch (foliageType) {
      case EARTH -> single("modernfoundry_earth_slime", TinkerStructures.earthSlimeTree);
      case SKY -> single("modernfoundry_sky_slime", TinkerStructures.skySlimeTree);
      case ENDER -> new TreeGrower("modernfoundry_ender_slime", 0.85f, Optional.empty(), Optional.empty(), Optional.of(TinkerStructures.enderSlimeTree), Optional.of(TinkerStructures.enderSlimeTreeTall), Optional.empty(), Optional.empty());
      case BLOOD -> single("modernfoundry_blood_slime", TinkerStructures.bloodSlimeFungus);
      case ICHOR -> single("modernfoundry_ichor_slime", TinkerStructures.ichorSlimeFungus);
    };
  }

  private static TreeGrower single(String name, ResourceKey<ConfiguredFeature<?, ?>> feature) {
    return new TreeGrower(name, Optional.empty(), Optional.of(feature), Optional.empty());
  }
}
