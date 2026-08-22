package modernmods.modernfoundry.integrations.client.integration.jei;

import java.util.Collections;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.items.TciItems;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.location;
import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;
import static modernmods.modernfoundry.integrations.util.TagHelper.getTag;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return resource("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IIngredientManager manager = jeiRuntime.getIngredientManager();

        removeIfNoIngotTag(manager, "desh", TciItems.MOLTEN_DESH.get(), TciItems.MOLTEN_DESH.getBucket());
        removeIfNoIngotTag(manager, "calorite", TciItems.MOLTEN_CALORITE.get(), TciItems.MOLTEN_CALORITE.getBucket());
        removeIfNoIngotTag(manager, "ostrum", TciItems.MOLTEN_OSTRUM.get(), TciItems.MOLTEN_OSTRUM.getBucket());

        if (!ModIntegration.canLoad(ModIntegration.BOTANIA_MODID)) {
            removeFluid(manager, TciItems.MOLTEN_MANASTEEL.get(), TciItems.MOLTEN_MANASTEEL.getBucket());
        }

        if (!ModIntegration.canLoad(ModIntegration.AQUACULTURE_MODID)) {
            removeFluid(manager, TciItems.MOLTEN_NEPTUNIUM.get(), TciItems.MOLTEN_NEPTUNIUM.getBucket());
        }

        if (!ModIntegration.canLoad(ModIntegration.MALUM_MODID)) {
            removeFluid(manager, TciItems.MOLTEN_SOUL_STAINED_STEEL.get(), TciItems.MOLTEN_SOUL_STAINED_STEEL.getBucket());
        }

        if (!ModIntegration.canLoad(ModIntegration.UNDERGARDEN_MODID)) {
            removeFluid(manager, TciItems.MOLTEN_CLOGGRUM.get(), TciItems.MOLTEN_CLOGGRUM.getBucket());
            removeFluid(manager, TciItems.MOLTEN_FROSTSTEEL.get(), TciItems.MOLTEN_FROSTSTEEL.getBucket());
            removeFluid(manager, TciItems.MOLTEN_FORGOTTEN_METAL.get(), TciItems.MOLTEN_FORGOTTEN_METAL.getBucket());
        }

        if (!ModIntegration.canLoad(ModIntegration.IFD_MODID)) {
            removeFluid(manager, TciItems.MOLTEN_DRAGONSTEEL_FIRE.get(), TciItems.MOLTEN_DRAGONSTEEL_FIRE.getBucket());
            removeFluid(manager, TciItems.MOLTEN_DRAGONSTEEL_ICE.get(), TciItems.MOLTEN_DRAGONSTEEL_ICE.getBucket());
            removeFluid(manager, TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING.get(), TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING.getBucket());
        }

        if (!ModIntegration.canLoad(ModIntegration.ARS_MODID)) {
            removeFluid(manager, TciItems.MOLTEN_SOURCE_GEM.get(), TciItems.MOLTEN_SOURCE_GEM.getBucket());
        }
    }

    private static void removeIfNoIngotTag(IIngredientManager manager, String name, Fluid fluid, Item bucket) {
        if (!getTag(location("forge", "ingots/" + name)).iterator().hasNext()) {
            removeFluid(manager, fluid, bucket);
        }
    }

    private static void removeFluid(IIngredientManager manager, Fluid fluid, Item bucket) {
        manager.removeIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK, Collections.singleton(new FluidStack(fluid, FluidType.BUCKET_VOLUME)));
        manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, Collections.singleton(new ItemStack(bucket)));
    }

}
