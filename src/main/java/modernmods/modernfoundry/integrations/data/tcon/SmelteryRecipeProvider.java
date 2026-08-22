package modernmods.modernfoundry.integrations.data.tcon;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.common.crafting.conditions.OrCondition;
import net.neoforged.neoforge.common.crafting.conditions.TagEmptyCondition;

import modernmods.hilt.recipe.data.ICommonRecipeHelper;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.registration.object.FluidObject;
import modernmods.modernfoundry.fluids.TinkerFluids;
import modernmods.modernfoundry.library.data.recipe.ISmelteryRecipeHelper;
import modernmods.modernfoundry.library.data.recipe.SmelteryRecipeBuilder;
import modernmods.modernfoundry.library.recipe.alloying.AlloyRecipeBuilder;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.recipe.casting.ItemCastingRecipeBuilder;
import modernmods.modernfoundry.library.recipe.melting.IMeltingContainer;
import modernmods.modernfoundry.library.recipe.melting.IMeltingRecipe;
import modernmods.modernfoundry.library.recipe.melting.MeltingRecipeBuilder;
import modernmods.modernfoundry.integrations.smeltery.TinkerSmeltery;

import modernmods.modernfoundry.integrations.data.BaseRecipeProvider;
import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.items.TciItems;
import modernmods.modernfoundry.integrations.util.ResourceLocationHelper;

import static modernmods.modernfoundry.library.data.recipe.SmelteryRecipeBuilder.itemTag;

public class SmelteryRecipeProvider extends BaseRecipeProvider implements ISmelteryRecipeHelper, ICommonRecipeHelper {

    public SmelteryRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "TciIntegration - Smeltery Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        this.addMeltingRecipes(consumer);
        this.addAlloyRecipes(consumer);
    }

    private void addMeltingRecipes(Consumer<FinishedRecipe> consumer) {
        String folder = "smeltery/melting/";

        // ores
        String metalFolder = folder + "metal/";
        Consumer<FinishedRecipe> botaniaConsumer = withCondition(consumer, modLoaded(ModIntegration.BOTANIA_MODID));
        Consumer<FinishedRecipe> aquacultureConsumer = withCondition(consumer, modLoaded(ModIntegration.AQUACULTURE_MODID));
        Consumer<FinishedRecipe> malumConsumer = withCondition(consumer, modLoaded(ModIntegration.MALUM_MODID));
        Consumer<FinishedRecipe> undergardenConsumer = withCondition(consumer, modLoaded(ModIntegration.UNDERGARDEN_MODID));
        Consumer<FinishedRecipe> adAstraConsumer = withCondition(consumer, new OrCondition(modLoaded(ModIntegration.AD_ASTRA_MODID), modLoaded(ModIntegration.BEYOND_EARTH_MODID)));
        Consumer<FinishedRecipe> ifdConsumer = withCondition(consumer, modLoaded(ModIntegration.IFD_MODID));
        Consumer<FinishedRecipe> arsConsumer = withCondition(consumer, modLoaded(ModIntegration.ARS_MODID));

        molten(arsConsumer, TciItems.MOLTEN_SOURCE_GEM).smallGem();
        metal(botaniaConsumer, TciItems.MOLTEN_MANASTEEL).metal();
        metal(aquacultureConsumer, TciItems.MOLTEN_NEPTUNIUM).metal();
        metal(malumConsumer, TciItems.MOLTEN_SOUL_STAINED_STEEL).metal();
        metal(undergardenConsumer, TciItems.MOLTEN_CLOGGRUM).ore().metal();
        metal(undergardenConsumer, TciItems.MOLTEN_FROSTSTEEL).ore().metal();
        metal(undergardenConsumer, TciItems.MOLTEN_FORGOTTEN_METAL).metal();
        metal(adAstraConsumer, TciItems.MOLTEN_DESH).ore().metal();
        metal(adAstraConsumer, TciItems.MOLTEN_CALORITE).ore().metal();
        metal(adAstraConsumer, TciItems.MOLTEN_OSTRUM).ore().metal();
        metalWithoutNugget(ifdConsumer, TciItems.MOLTEN_DRAGONSTEEL_FIRE);
        metalWithoutNugget(ifdConsumer, TciItems.MOLTEN_DRAGONSTEEL_ICE);
        metalWithoutNugget(ifdConsumer, TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING);

        // IFD Silver Items
        // armor
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_SILVER_METAL_HELMET), TinkerFluids.moltenSilver.get(), FluidValues.INGOT * 5)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/helmet"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_SILVER_METAL_CHESTPLATE), TinkerFluids.moltenSilver.get(), FluidValues.INGOT * 8)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/chestplate"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_SILVER_METAL_LEGGINGS), TinkerFluids.moltenSilver.get(), FluidValues.INGOT * 7)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/leggings"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_SILVER_METAL_BOOTS), TinkerFluids.moltenSilver.get(), FluidValues.INGOT * 4)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/boots"));
        // tools
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_SILVER_AXE, ModIntegration.IFD_SILVER_PICKAXE), TinkerFluids.moltenSilver.get(), FluidValues.INGOT * 3)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/axes"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_SILVER_SWORD, ModIntegration.IFD_SILVER_HOE), TinkerFluids.moltenSilver.get(), FluidValues.INGOT * 2)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/weapon"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_SILVER_SHOVEL), TinkerFluids.moltenSilver.get(), FluidValues.INGOT)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/small"));
        // Dragon Armor
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_DRAGONARMOR_SILVER_HEAD, ModIntegration.IFD_DRAGONARMOR_SILVER_NECK), TinkerFluids.moltenSilver.get(), FluidValues.METAL_BLOCK * 5)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/dragon_armor_head_neck"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_DRAGONARMOR_SILVER_BODY), TinkerFluids.moltenSilver.get(), FluidValues.METAL_BLOCK * 8)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/dragon_armor_head_body"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_DRAGONARMOR_SILVER_TAIL), TinkerFluids.moltenSilver.get(), FluidValues.METAL_BLOCK * 3)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "silver/dragon_armor_head_tail"));

        // IFD Copper Items
        // armor
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_COPPER_METAL_HELMET), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 5)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/helmet"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_COPPER_METAL_CHESTPLATE), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 8)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/chestplate"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_COPPER_METAL_LEGGINGS), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 7)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/leggings"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_COPPER_METAL_BOOTS), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 4)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/boots"));
        // tools
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_COPPER_AXE, ModIntegration.IFD_COPPER_PICKAXE), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 3)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/axes"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_COPPER_SWORD, ModIntegration.IFD_COPPER_HOE), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 2)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/weapon"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_COPPER_SHOVEL), TinkerFluids.moltenCopper.get(), FluidValues.INGOT)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/small"));
        // Dragon Armor
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_DRAGONARMOR_COPPER_HEAD, ModIntegration.IFD_DRAGONARMOR_COPPER_NECK), TinkerFluids.moltenCopper.get(), FluidValues.METAL_BLOCK * 5)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/dragon_armor_head_neck"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_DRAGONARMOR_COPPER_BODY), TinkerFluids.moltenCopper.get(), FluidValues.METAL_BLOCK * 8)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/dragon_armor_head_body"));
        MeltingRecipeBuilder.melting(Ingredient.of(ModIntegration.IFD_DRAGONARMOR_COPPER_TAIL), TinkerFluids.moltenCopper.get(), FluidValues.METAL_BLOCK * 3)
            .setDamagable(FluidValues.NUGGET)
            .save(ifdConsumer, location(metalFolder + "copper/dragon_armor_head_tail"));
    }

    private void addAlloyRecipes(Consumer<FinishedRecipe> consumer) {
        String folder = "smeltery/alloys/";

        // Update Recipe to use Obsidian instead of Quartz to not interfere with Hepatizon
        Consumer<FinishedRecipe> wrapped = withCondition(consumer, new TagEmptyCondition(ResourceLocationHelper.location("forge", "ingots/tin")));

        AlloyRecipeBuilder.alloy(TinkerFluids.moltenBronze.get(), FluidValues.INGOT * 4)
            .addInput(TinkerFluids.moltenCopper.ingredient(FluidValues.INGOT * 3))
            .addInput(TinkerFluids.moltenObsidian.ingredient(FluidValues.GLASS_BLOCK))
            .save(wrapped, prefix(TinkerFluids.moltenBronze, folder));
    }

    /** Creates a metal from a tag */
    public SmelteryRecipeBuilder metal(Consumer<FinishedRecipe> consumer, String name, TagKey<Fluid> fluid) {
        return SmelteryRecipeBuilder.fluid(consumer, location(name), fluid).castingFolder("smeltery/casting/metal").meltingFolder("smeltery/melting/metal");
    }

    /** Creates a smeltery builder for a metal fluid */
    public SmelteryRecipeBuilder metal(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid) {
        return molten(consumer, fluid).castingFolder("smeltery/casting/metal").meltingFolder("smeltery/melting/metal");
    }

    private void metalWithoutNugget(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid) {
        SmelteryRecipeBuilder builder = molten(consumer, fluid).castingFolder("smeltery/casting/metal").meltingFolder("smeltery/melting/metal");
        ResourceLocation name = this.location(fluid.getId().getPath().substring("molten_".length()));

        builder.oreRate(IMeltingContainer.OreRateType.METAL);
        builder.baseUnit(90);
        builder.damageUnit(10);
        builder.melting(9.0F, "block", "storage_blocks", 3.0F, false, false);
        basinMetalCasting(builder, consumer, fluid,  name);
        builder.meltingCasting(1.0F, TinkerSmeltery.ingotCast, 1.0F, false);
    }

    private void basinMetalCasting(SmelteryRecipeBuilder builder, Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, ResourceLocation name) {
        String castingFolder = "smeltery/casting/metal/";
        String tagName = "storage_blocks/" + name.getPath();

        ItemCastingRecipeBuilder.basinRecipe(ItemOutput.fromTag(itemTag(tagName))).setFluid(fluid.ingredient(810)).setCoolingTime(IMeltingRecipe.getTemperature(fluid), 810).save(consumer, location(name, castingFolder, "block"));
    }

    private ResourceLocation location(ResourceLocation name, String folder, String variant) {
        return name.withPath(folder + name.getPath() + "/" + variant);
    }

}
