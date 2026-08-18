package modernmods.modernfoundry.world;

import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.color.block.BlockTintSource;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import modernmods.modernfoundry.world.client.SlimeFoliageTintSource;

import java.util.List;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.ClientEventBase;
import modernmods.modernfoundry.library.client.particle.SlimeParticle;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.shared.block.SlimeType;
import modernmods.modernfoundry.tools.client.SlimeskullArmorModel;
import modernmods.modernfoundry.tools.data.material.MaterialIds;
import modernmods.modernfoundry.world.block.FoliageType;
import modernmods.modernfoundry.world.client.DragonSkullModel;
import modernmods.modernfoundry.world.client.SkullModelHelper;
import modernmods.modernfoundry.world.client.SlimeColorReloadListener;
import modernmods.modernfoundry.world.client.SlimeColorizer;
import modernmods.modernfoundry.world.client.TerracubeRenderer;
import modernmods.modernfoundry.world.client.TinkerSlimeRenderer;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@EventBusSubscriber(modid=TConstruct.MOD_ID, value=Dist.CLIENT)
public class WorldClientEvents extends ClientEventBase {
  // 26.1.2: client render overhaul — AddClientReloadListenersEvent was renamed to
  // AddClientReloadListenersEvent (a SortedReloadListenerEvent); listeners are registered by name via addListener.
  @SubscribeEvent
  static void addResourceListener(AddClientReloadListenersEvent event) {
    for (FoliageType type : FoliageType.values()) {
      event.addListener(TConstruct.getResource("slime_color/" + type.getSerializedName()), new SlimeColorReloadListener(type));
    }
  }

  @SubscribeEvent
  static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    event.registerSpecial(TinkerWorld.skySlimeParticle.get(), new SlimeParticle.Factory(SlimeType.SKY));
    event.registerSpecial(TinkerWorld.enderSlimeParticle.get(), new SlimeParticle.Factory(SlimeType.ENDER));
    event.registerSpecial(TinkerWorld.terracubeParticle.get(), new SlimeParticle.Factory(Items.CLAY_BALL));
  }

  @SubscribeEvent
  static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
    // slime foliage tint index 0 is colored by world position (SlimeColorizer). The pre-26.1 BlockColors position
    // handler was removed, so register a BlockTintSource instance per foliage type instead.
    for (FoliageType type : FoliageType.values()) {
      // grass blocks (every dirt variant) and short plants sample at their own position
      event.register(List.<BlockTintSource>of(new SlimeFoliageTintSource(type, null)),
        TinkerWorld.vanillaSlimeGrass.get(type), TinkerWorld.earthSlimeGrass.get(type), TinkerWorld.skySlimeGrass.get(type),
        TinkerWorld.enderSlimeGrass.get(type), TinkerWorld.ichorSlimeGrass.get(type),
        TinkerWorld.slimeFern.get(type), TinkerWorld.slimeTallGrass.get(type));
      // leaves sample with the loop offset so they differ from the grass below
      event.register(List.<BlockTintSource>of(new SlimeFoliageTintSource(type, SlimeColorizer.LOOP_OFFSET)),
        TinkerWorld.slimeLeaves.get(type));
    }
    // vines are keyed by slime type; color them to match their foliage with the loop offset
    event.register(List.<BlockTintSource>of(new SlimeFoliageTintSource(FoliageType.SKY, SlimeColorizer.LOOP_OFFSET)),
      TinkerWorld.skySlimeVine.get());
    event.register(List.<BlockTintSource>of(new SlimeFoliageTintSource(FoliageType.ENDER, SlimeColorizer.LOOP_OFFSET)),
      TinkerWorld.enderSlimeVine.get());
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    // TODO: do we really need a separate copy of each head for each mob, or can we reuse them?
    Supplier<LayerDefinition> normalHead = Lazy.of(SkullModel::createMobHeadLayer);
    Supplier<LayerDefinition> customHead = Lazy.of(() -> SkullModelHelper.createHeadLayer(0, 0, 32, 16));
    Supplier<LayerDefinition> headOverlayCustom = Lazy.of(() -> SkullModelHelper.createHeadHatLayer(0, 16, 32, 32));
    registerLayerDefinition(event, TinkerHeadType.BLAZE, normalHead);
    registerLayerDefinition(event, TinkerHeadType.ENDERMAN, customHead);
    registerLayerDefinition(event, TinkerHeadType.STRAY, headOverlayCustom);

    // zombie
    registerLayerDefinition(event, TinkerHeadType.HUSK, Lazy.of(() -> SkullModelHelper.createHeadLayer(0, 0, 64, 64)));
    registerLayerDefinition(event, TinkerHeadType.DROWNED, headOverlayCustom);

    // spiders
    Supplier<LayerDefinition> spiderHead = Lazy.of(() -> SkullModelHelper.createHeadLayer(32, 4, 64, 32));
    registerLayerDefinition(event, TinkerHeadType.SPIDER, spiderHead);
    registerLayerDefinition(event, TinkerHeadType.CAVE_SPIDER, spiderHead);

    // piglin
    Supplier<LayerDefinition> piglinHead = Lazy.of(() -> LayerDefinition.create(PiglinHeadModel.createHeadModel(), 64, 64));
    registerLayerDefinition(event, TinkerHeadType.PIGLIN_BRUTE, piglinHead);
    registerLayerDefinition(event, TinkerHeadType.ZOMBIFIED_PIGLIN, piglinHead);

    // crafted
    registerLayerDefinition(event, TinkerHeadType.VENOMBONE, customHead);
    registerLayerDefinition(event, TinkerHeadType.BLAZING_BONE, customHead);
    registerLayerDefinition(event, TinkerHeadType.NECRONIUM, customHead);
    event.registerLayerDefinition(SkullModelHelper.FLUID_CANNON, headOverlayCustom);
  }

  // 26.1.2: client render overhaul — modded skull blocks are resolved through the model + texture registered here on
  // CreateSkullModels (SkullBlockRenderer.createModel delegates modded types to ClientHooks#getModdedSkullModel, and the
  // texture replaces the removed SkullBlockRenderer.SKIN_BY_TYPE lookup). Piglin heads use PiglinHeadModel; the rest use
  // the vanilla SkullModel over the per-type layer registered in registerRenderers above.
  @SubscribeEvent
  static void registerSkullModels(EntityRenderersEvent.CreateSkullModels event) {
    for (TinkerHeadType type : TinkerHeadType.values()) {
      Identifier texture = SkullModelHelper.HEAD_TEXTURES.get(type);
      if (texture == null) {
        continue;
      }
      ModelLayerLocation layer = SkullModelHelper.HEAD_LAYERS.get(type);
      switch (type) {
        case PIGLIN_BRUTE, ZOMBIFIED_PIGLIN -> event.registerSkullModel(type, modelSet -> new PiglinHeadModel(modelSet.bakeLayer(layer)), texture);
        default -> event.registerSkullModel(type, modelSet -> new SkullModel(modelSet.bakeLayer(layer)), texture);
      }
    }
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(TinkerWorld.skySlimeEntity.get(), TinkerSlimeRenderer.SKY_SLIME_FACTORY);
    event.registerEntityRenderer(TinkerWorld.enderSlimeEntity.get(), TinkerSlimeRenderer.ENDER_SLIME_FACTORY);
    event.registerEntityRenderer(TinkerWorld.terracubeEntity.get(), TerracubeRenderer::new);
  }

  @SuppressWarnings("removal")
  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    // skull textures
    event.enqueueWork(() -> {
      registerHeadModel(TinkerHeadType.BLAZE, MaterialIds.blaze, Identifier.parse("textures/entity/blaze.png"));
      registerHeadModel(TinkerHeadType.ENDERMAN, MaterialIds.enderPearl, TConstruct.getResource("textures/entity/skull/enderman.png"));
      SlimeskullArmorModel.registerHeadModel(MaterialIds.dragonScale, modelSet -> new DragonSkullModel(modelSet.bakeLayer(ModelLayers.DRAGON_SKULL)), Identifier.parse("textures/entity/enderdragon/dragon.png"));
      SlimeskullArmorModel.registerHeadModel(MaterialIds.glass, ModelLayers.CREEPER_HEAD, Identifier.parse("textures/entity/creeper/creeper.png"));
      // skeleton
      SlimeskullArmorModel.registerHeadModel(MaterialIds.bone, ModelLayers.SKELETON_SKULL, Identifier.parse("textures/entity/skeleton/skeleton.png"));
      SlimeskullArmorModel.registerHeadModel(MaterialIds.necroticBone, ModelLayers.WITHER_SKELETON_SKULL, Identifier.parse("textures/entity/skeleton/wither_skeleton.png"));
      registerHeadModel(TinkerHeadType.STRAY, MaterialIds.ice, TConstruct.getResource("textures/entity/skull/stray.png"));
      // zombies
      SlimeskullArmorModel.registerHeadModel(MaterialIds.leather, ModelLayers.ZOMBIE_HEAD, Identifier.parse("textures/entity/zombie/zombie.png"));
      registerHeadModel(TinkerHeadType.HUSK, MaterialIds.iron, Identifier.parse("textures/entity/zombie/husk.png"));
      registerHeadModel(TinkerHeadType.DROWNED, MaterialIds.copper, TConstruct.getResource("textures/entity/skull/drowned.png"));
      // spider
      registerHeadModel(TinkerHeadType.SPIDER, MaterialIds.string, Identifier.parse("textures/entity/spider/spider.png"));
      registerHeadModel(TinkerHeadType.CAVE_SPIDER, MaterialIds.darkthread, Identifier.parse("textures/entity/spider/cave_spider.png"));
      // piglins
      SlimeskullArmorModel.registerPiglinHeadModel(MaterialIds.gold, ModelLayers.PIGLIN_HEAD, Identifier.parse("textures/entity/piglin/piglin.png"));
      registerPiglinHeadModel(TinkerHeadType.PIGLIN_BRUTE, MaterialIds.roseGold, Identifier.parse("textures/entity/piglin/piglin_brute.png"));
      registerPiglinHeadModel(TinkerHeadType.ZOMBIFIED_PIGLIN, MaterialIds.pigIron, Identifier.parse("textures/entity/piglin/zombified_piglin.png"));
      // crafted
      registerHeadModel(TinkerHeadType.VENOMBONE,    MaterialIds.venombone,   TConstruct.getResource("textures/entity/skull/venombone.png"));
      registerHeadModel(TinkerHeadType.BLAZING_BONE, MaterialIds.blazingBone, TConstruct.getResource("textures/entity/skull/blazing_bone.png"));
      registerHeadModel(TinkerHeadType.NECRONIUM,    MaterialIds.necronium,   TConstruct.getResource("textures/entity/skull/necronium.png"));
      SlimeskullArmorModel.registerHeadModel(MaterialIds.knightmetal, SkullModelHelper.FLUID_CANNON, TConstruct.getResource("textures/entity/skull/fluid_cannon.png"));
    });
  }

  // 26.1.2: client render overhaul — the block/item color-handler system was replaced. RegisterColorHandlersEvent.Block
  // and .Item (and net.minecraft.client.color.item.ItemColors) are gone; block/item tinting is now data-driven through
  // RegisterColorHandlersEvent.BlockTintSources / ItemTintSources / ColorResolvers (tint-source types referenced from
  // model JSON). The slime-foliage position colors below (getSlimeColorByPos / SlimeColorizer) must be reimplemented as
  // a registered BlockTintSource + matching ItemTintSource during the render pass. Registration handlers omitted here so
  // the mod still loads; see getSlimeColorByPos for the preserved color logic.

  /**
   * Block colors for a slime type
   * @param pos   Block position
   * @param type  Slime foliage color
   * @param add   Offset position
   * @return  Color for the given position, or the default if position is null
   */
  private static int getSlimeColorByPos(@Nullable BlockPos pos, FoliageType type, @Nullable BlockPos add) {
    if (pos == null) {
      return type.getColor();
    }
    if (add != null) {
      pos = pos.offset(add);
    }

    return SlimeColorizer.getColorForPos(pos, type);
  }

  /** Registers a skull with the entity renderer and the slimeskull renderer */
  private static void registerHeadModel(TinkerHeadType skull, MaterialId materialId, Identifier texture) {
    // 26.1.2: SkullBlockRenderer.SKIN_BY_TYPE was removed; skull-block textures are now supplied via
    // CreateSkullModels#registerSkullModel(type, layer, texture) (see registerSkullModels above).
    SlimeskullArmorModel.registerHeadModel(materialId, SkullModelHelper.HEAD_LAYERS.get(skull), texture);
  }

  /** Registers a skull with the entity renderer and the slimeskull renderer */
  private static void registerPiglinHeadModel(TinkerHeadType skull, MaterialId materialId, Identifier texture) {
    // 26.1.2: SkullBlockRenderer.SKIN_BY_TYPE removed — see registerHeadModel note.
    SlimeskullArmorModel.registerPiglinHeadModel(materialId, SkullModelHelper.HEAD_LAYERS.get(skull), texture);
  }

  /** Register a layer without being under the minecraft domain. TODO: is this needed? */
  private static ModelLayerLocation registerLayer(String name) {
    ModelLayerLocation location = new ModelLayerLocation(TConstruct.getResource(name), "main");
    if (!ModelLayers.ALL_MODELS.add(location)) {
      throw new IllegalStateException("Duplicate registration for " + location);
    } else {
      return location;
    }
  }

  /** Register a head layer definition with forge */
  private static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event, TinkerHeadType head, Supplier<LayerDefinition> supplier) {
    event.registerLayerDefinition(SkullModelHelper.HEAD_LAYERS.get(head), supplier);
  }
}
