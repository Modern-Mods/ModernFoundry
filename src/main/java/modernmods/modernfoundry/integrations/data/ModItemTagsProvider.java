package modernmods.modernfoundry.integrations.data;

import modernmods.modernfoundry.TConstruct;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.Tags;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.integrations.shared.TinkerMaterials;

import modernmods.modernfoundry.integrations.common.TagManager;
import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.items.TciItems;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, ModBlockTagsProvider blockTagsProvider, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), TConstruct.MOD_ID, existingFileHelper);
    }

    @Override
    public @NotNull String getName() {
        return "TciIntegration - Item Tags";
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.copy(TagManager.Blocks.BRONZE, TagManager.Items.BRONZE);

        this.tag(Tags.Items.INGOTS)
            .add(TciItems.BRONZE.getIngot())
            .addOptional(ModIntegration.malumLoc("soul_stained_steel_ingot"));
        this.tag(Tags.Items.NUGGETS)
            .add(TciItems.BRONZE.getNugget())
            .addOptional(ModIntegration.malumLoc("soul_stained_steel_nugget"));

        this.tag(TagManager.Items.BRONZE_INGOTS).add(TciItems.BRONZE.getIngot());
        this.tag(TagManager.Items.BRONZE_NUGGETS).add(TciItems.BRONZE.getNugget());

        // Botania
        addBotaniaLogVariants(TagManager.Items.BOTANIA_LIVINGWOOD_LOGS, "livingwood");
        this.tag(TinkerTags.Items.VARIANT_LOGS).addOptionalTag(TagManager.Items.BOTANIA_LIVINGWOOD_LOGS.location());
        this.tag(TinkerTags.Items.VARIANT_PLANKS).addOptional(ModIntegration.botaniaLoc("livingwood_planks"));

        // Malum
        this.copy(TagManager.Blocks.SOUL_STAINED_STEEL, TagManager.Items.SOUL_STAINED_STEEL);
        this.tag(TagManager.Items.SOUL_STAINED_STEEL_INGOTS).addOptional(ModIntegration.malumLoc("soul_stained_steel_ingot"));
        this.tag(TagManager.Items.SOUL_STAINED_STEEL_NUGGETS).addOptional(ModIntegration.malumLoc("soul_stained_steel_nugget"));

        // Ice and Fire: Dragons
        this.tag(TagManager.Items.WITHER_BONES)
            .add(TinkerMaterials.necroticBone.get())
            .addOptional(ModIntegration.ifdLoc("witherbone"));
        this.copy(TagManager.Blocks.DRAGONSTEEL_FIRE, TagManager.Items.DRAGONSTEEL_FIRE);
        this.tag(TagManager.Items.DRAGONSTEEL_FIRE_INGOTS).addOptional(ModIntegration.ifdLoc("dragonsteel_fire_ingot"));
        this.copy(TagManager.Blocks.DRAGONSTEEL_ICE, TagManager.Items.DRAGONSTEEL_ICE);
        this.tag(TagManager.Items.DRAGONSTEEL_ICE_INGOTS).addOptional(ModIntegration.ifdLoc("dragonsteel_ice_ingot"));
        this.copy(TagManager.Blocks.DRAGONSTEEL_LIGHTNING, TagManager.Items.DRAGONSTEEL_LIGHTNING);
        this.tag(TagManager.Items.DRAGONSTEEL_LIGHTNING_INGOTS).addOptional(ModIntegration.ifdLoc("dragonsteel_lightning_ingot"));

        // Ad Astra & Beyond Earth
        this.tag(TagManager.Items.CHEESE)
            .addOptional(ModIntegration.adAstraLoc("cheese"))
            .addOptional(ModIntegration.beyondEarthLoc("cheese"));

        // Ars Nouveau
        this.tag(TagManager.Items.SOURCE_GEM)
            .addOptional(ModIntegration.arsLoc("source_gem"));
        this.tag(TagManager.Items.SOURCE_GEM_BLOCK)
            .addOptional(ModIntegration.arsLoc("source_gem_block"));
    }

    private void addBotaniaLogVariants(TagKey<Item> tag, String type) {
        this.tag(tag)
            .addOptional(ModIntegration.botaniaLoc(type + "_log"))
            .addOptional(ModIntegration.botaniaLoc("stripped_" + type + "_log"))
            .addOptional(ModIntegration.botaniaLoc(type))
            .addOptional(ModIntegration.botaniaLoc("stripped_" + type));
    }

}
