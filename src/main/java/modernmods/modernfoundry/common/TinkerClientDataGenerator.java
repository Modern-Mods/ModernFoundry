package modernmods.modernfoundry.common;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataGenerator.PackGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.data.material.MaterialPaletteDebugGenerator;
import modernmods.modernfoundry.library.client.data.material.GeneratorPartTextureJsonGenerator;
import modernmods.modernfoundry.tools.data.material.MaterialRenderInfoProvider;
import modernmods.modernfoundry.tools.data.sprite.TinkerMaterialSpriteProvider;
import modernmods.modernfoundry.tools.data.sprite.TinkerPartSpriteProvider;
import modernmods.modernfoundry.tools.data.sprite.TinkerTrimMaterialPaletteGenerator;

import java.util.concurrent.CompletableFuture;

/**
 * Registers the CLIENT-side (assets) data generators on {@link GatherDataEvent.Client}.
 * <p>
 * 26.1 split datagen into {@link GatherDataEvent.Client} and {@code GatherDataEvent.Server}; the server providers
 * (recipes/loot/tags/advancements) are wired separately so the two halves stay independent. This class only touches
 * client assets: material render info, the part-texture generator manifest, trim palettes and the debug palette dump.
 * Sprite/texture generators read existing sprites through {@link GatherDataEvent#getResourceManager(PackType)} now that
 * {@code ExistingFileHelper} is gone.
 */
@EventBusSubscriber(modid = TConstruct.MOD_ID)
public class TinkerClientDataGenerator {
  private TinkerClientDataGenerator() {}

  @SubscribeEvent
  static void gatherData(GatherDataEvent.Client event) {
    DataGenerator generator = event.getGenerator();
    ResourceManager resourceManager = event.getResourceManager(PackType.CLIENT_RESOURCES);
    @SuppressWarnings("unused")
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

    // shared sprite metadata, drives both the render info colors/fallbacks and the part-texture manifest
    TinkerMaterialSpriteProvider materialSprites = new TinkerMaterialSpriteProvider();
    TinkerPartSpriteProvider partSprites = new TinkerPartSpriteProvider();

    PackGenerator pack = generator.getVanillaPack(true);
    // assets/modernfoundry/materials/... render info json
    pack.addProvider(output -> new MaterialRenderInfoProvider(output, materialSprites, resourceManager));
    // assets/modernfoundry/tinkering/generator_part_textures.json (consumed by the in-game part texture command)
    pack.addProvider(output -> new GeneratorPartTextureJsonGenerator(output, TConstruct.MOD_ID, partSprites));
    // trims/color_palettes recolored per tinker material + the trimmed compat json
    pack.addProvider(output -> new TinkerTrimMaterialPaletteGenerator(output, resourceManager, materialSprites));
    // debug palette dump (excluded from the built jar via assets/modernfoundry/debug)
    pack.addProvider(output -> new MaterialPaletteDebugGenerator(output, "Tinkers' Construct", materialSprites));
  }
}
