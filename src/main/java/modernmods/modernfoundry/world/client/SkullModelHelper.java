package modernmods.modernfoundry.world.client;

import net.minecraft.util.Util;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.world.TinkerHeadType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Helps with creation and registration of skull block models */
public class SkullModelHelper {
  /** Map of head type to model layer location for each head type */
  public static final Map<TinkerHeadType,ModelLayerLocation> HEAD_LAYERS = Arrays.stream(TinkerHeadType.values()).collect(
    Collectors.toMap(Function.identity(), type -> new ModelLayerLocation(TConstruct.getResource(type.getSerializedName() + "_head"), "main"), (a, b) -> a, () -> new EnumMap<>(TinkerHeadType.class)));
  /** Skull-block texture for each head type, threaded into the 26.1 skull renderer via CreateSkullModels */
  public static final Map<TinkerHeadType,Identifier> HEAD_TEXTURES = Util.make(new EnumMap<>(TinkerHeadType.class), map -> {
    map.put(TinkerHeadType.BLAZE,            Identifier.parse("textures/entity/blaze.png"));
    map.put(TinkerHeadType.ENDERMAN,         TConstruct.getResource("textures/entity/skull/enderman.png"));
    map.put(TinkerHeadType.STRAY,            TConstruct.getResource("textures/entity/skull/stray.png"));
    map.put(TinkerHeadType.HUSK,             Identifier.parse("textures/entity/zombie/husk.png"));
    map.put(TinkerHeadType.DROWNED,          TConstruct.getResource("textures/entity/skull/drowned.png"));
    map.put(TinkerHeadType.SPIDER,           Identifier.parse("textures/entity/spider/spider.png"));
    map.put(TinkerHeadType.CAVE_SPIDER,      Identifier.parse("textures/entity/spider/cave_spider.png"));
    map.put(TinkerHeadType.PIGLIN_BRUTE,     Identifier.parse("textures/entity/piglin/piglin_brute.png"));
    map.put(TinkerHeadType.ZOMBIFIED_PIGLIN, Identifier.parse("textures/entity/piglin/zombified_piglin.png"));
    map.put(TinkerHeadType.VENOMBONE,        TConstruct.getResource("textures/entity/skull/venombone.png"));
    map.put(TinkerHeadType.BLAZING_BONE,     TConstruct.getResource("textures/entity/skull/blazing_bone.png"));
    map.put(TinkerHeadType.NECRONIUM,        TConstruct.getResource("textures/entity/skull/necronium.png"));
  });
  /** Model layer for the fluid cannon */
  public static final ModelLayerLocation FLUID_CANNON = new ModelLayerLocation(TConstruct.getResource("fluid_cannon_skull"), "main");

  private SkullModelHelper() {}

  /** Creates a head with the given start and texture size */
  public static LayerDefinition createHeadLayer(int headX, int headY, int width, int height) {
    MeshDefinition mesh = new MeshDefinition();
    mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(headX, headY).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
    return LayerDefinition.create(mesh, width, height);
  }

  /** Creates a head with a hat, starting the head at 0,0, hat at the values, and using the given size */
  @SuppressWarnings("SameParameterValue")
  public static LayerDefinition createHeadHatLayer(int hatX, int hatY, int width, int height) {
    MeshDefinition mesh = SkullModel.createHeadModel();
    mesh.getRoot().getChild("head").addOrReplaceChild("hat", CubeListBuilder.create().texOffs(hatX, hatY).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.ZERO);
    return LayerDefinition.create(mesh, width, height);
  }
}
