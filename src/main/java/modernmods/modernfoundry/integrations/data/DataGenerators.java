package modernmods.modernfoundry.integrations.data;

import modernmods.modernfoundry.TConstruct;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import modernmods.modernfoundry.fluids.data.FluidBlockstateModelProvider;
import modernmods.modernfoundry.fluids.data.FluidBucketModelProvider;

import modernmods.modernfoundry.integrations.data.client.ModBlockStateProvider;
import modernmods.modernfoundry.integrations.data.client.ModItemModelProvider;
import modernmods.modernfoundry.integrations.data.integration.ProjectEConversionProvider;
import modernmods.modernfoundry.integrations.data.loot.ModLootTables;
import modernmods.modernfoundry.integrations.data.recipes.ModRecipesProvider;
import modernmods.modernfoundry.integrations.data.tcon.EnchantmentToModifierProvider;
import modernmods.modernfoundry.integrations.data.tcon.ModifierTagProvider;
import modernmods.modernfoundry.integrations.data.tcon.fluid.FluidEffectProvider;
import modernmods.modernfoundry.integrations.data.tcon.fluid.FluidTagProvider;
import modernmods.modernfoundry.integrations.data.tcon.fluid.FluidTextureProvider;
import modernmods.modernfoundry.integrations.data.tcon.ModifierProvider;
import modernmods.modernfoundry.integrations.data.tcon.ModifierRecipeProvider;
import modernmods.modernfoundry.integrations.data.tcon.material.MaterialDataProvider;
import modernmods.modernfoundry.integrations.data.tcon.material.MaterialRecipeProvider;
import modernmods.modernfoundry.integrations.data.tcon.material.MaterialRenderInfoProvider;
import modernmods.modernfoundry.integrations.data.tcon.material.MaterialStatsDataProvider;
import modernmods.modernfoundry.integrations.data.tcon.material.MaterialTraitsDataProvider;
import modernmods.modernfoundry.integrations.data.tcon.sprite.TinkerMaterialSpriteProvider;
import modernmods.modernfoundry.integrations.data.tcon.SmelteryRecipeProvider;

@Mod.EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {

    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        ModBlockTagsProvider blockTags = new ModBlockTagsProvider(packOutput, event.getLookupProvider(), existingFileHelper);
        TinkerMaterialSpriteProvider materialSprites = new TinkerMaterialSpriteProvider();
        MaterialDataProvider materials = new MaterialDataProvider(packOutput);
        boolean server = event.includeServer();
        boolean client = event.includeClient();

        gen.addProvider(server, new ModifierRecipeProvider(packOutput));
        gen.addProvider(server, new ModItemModelProvider(packOutput, existingFileHelper));
        gen.addProvider(server, new ModBlockStateProvider(packOutput, existingFileHelper));
        gen.addProvider(server, blockTags);
        gen.addProvider(server, new ModItemTagsProvider(packOutput, event.getLookupProvider(), blockTags, existingFileHelper));
        gen.addProvider(server, new ModRecipesProvider(packOutput));
        gen.addProvider(server, ModLootTables.create(packOutput));
        gen.addProvider(server, new EntityTypeTagProvider(packOutput, event.getLookupProvider(), existingFileHelper));
        gen.addProvider(server, new FluidTagProvider(packOutput, event.getLookupProvider(), existingFileHelper));
        gen.addProvider(client, new MaterialRenderInfoProvider(packOutput, materialSprites, existingFileHelper));
        gen.addProvider(server, new MaterialStatsDataProvider(packOutput, materials));
        gen.addProvider(server, new MaterialTraitsDataProvider(packOutput, materials));
        gen.addProvider(server, new MaterialRecipeProvider(packOutput));
        gen.addProvider(server, new SmelteryRecipeProvider(packOutput));
        gen.addProvider(server, materials);
        gen.addProvider(server, new ProjectEConversionProvider(packOutput, event.getLookupProvider()));
        gen.addProvider(server, new ModifierProvider(packOutput));
        gen.addProvider(client, new FluidTextureProvider(packOutput));
        gen.addProvider(client, new FluidBlockstateModelProvider(packOutput, TConstruct.MOD_ID));
        gen.addProvider(client, new FluidBucketModelProvider(packOutput, TConstruct.MOD_ID));
        gen.addProvider(server, new FluidEffectProvider(packOutput, TConstruct.MOD_ID));
        gen.addProvider(server, new ModifierTagProvider(packOutput, TConstruct.MOD_ID, existingFileHelper));
        gen.addProvider(server, new EnchantmentToModifierProvider(packOutput));
    }

}