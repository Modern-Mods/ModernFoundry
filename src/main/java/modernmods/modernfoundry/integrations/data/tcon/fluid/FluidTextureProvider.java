package modernmods.modernfoundry.integrations.data.tcon.fluid;

import modernmods.modernfoundry.TConstruct;

import org.jetbrains.annotations.NotNull;

import net.minecraft.data.PackOutput;

import modernmods.hilt.fluid.texture.AbstractFluidTextureProvider;
import modernmods.hilt.fluid.texture.FluidTexture;
import modernmods.hilt.registration.object.FluidObject;

import modernmods.modernfoundry.integrations.items.TciItems;

import static modernmods.modernfoundry.TConstruct.getResource;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public class FluidTextureProvider extends AbstractFluidTextureProvider {

    public FluidTextureProvider(PackOutput packOutput) {
        super(packOutput, TConstruct.MOD_ID);
    }

    @Override
    public @NotNull String getName() {
        return "TciIntegration - TCon Fluid Textures";
    }

    @Override
    public void addTextures() {
        tintedStone(TciItems.MOLTEN_CLOGGRUM).color(0xFFB79A7A);
        compatAlloy(TciItems.MOLTEN_FORGOTTEN_METAL); // metal_still - metal_flow
        compatAlloy(TciItems.MOLTEN_FROSTSTEEL); // metal_still - metal_flow
        compatAlloy(TciItems.MOLTEN_MANASTEEL); // metal_shiny_still - metal_shiny_flow
        compatAlloy(TciItems.MOLTEN_NEPTUNIUM); // metal_still - metal_flow
        compatAlloy(TciItems.MOLTEN_SOUL_STAINED_STEEL); // metal_still - metal_flow
        compatOre(TciItems.MOLTEN_SOURCE_GEM); // crystal_still - crystal_flow
        tintedStone(TciItems.MOLTEN_DESH).color(0xFFCD7F48);
        tintedStone(TciItems.MOLTEN_CALORITE).color(0xFFC24148);
        tintedStone(TciItems.MOLTEN_OSTRUM).color(0xFF73515E);
        compatAlloy(TciItems.MOLTEN_DRAGONSTEEL_FIRE); // metal_still - metal_flow
        compatAlloy(TciItems.MOLTEN_DRAGONSTEEL_ICE); // metal_still - metal_flow
        compatAlloy(TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING); // metal_still - metal_flow
    }

    private FluidTexture.Builder named(FluidObject<?> fluid, String name) {
        return texture(fluid).textures(resource("fluid/" + name + "/"), false, false);
    }

    private FluidTexture.Builder namedTcon(FluidObject<?> fluid, String name) {
        return texture(fluid).textures(getResource("fluid/" + name + "/"), false, false);
    }

    private FluidTexture.Builder moltenFolder(FluidObject<?> fluid, String folder) {
        return named(fluid, "molten/" + folder + "/" + fluid.getId().getPath());
    }

    private FluidTexture.Builder compatAlloy(FluidObject<?> fluid) {
        return moltenFolder(fluid, "compat_alloy");
    }

    private FluidTexture.Builder compatOre(FluidObject<?> fluid) {
        return moltenFolder(fluid, "compat_ore");
    }

    private FluidTexture.Builder tintedStone(FluidObject<?> fluid) {
        return namedTcon(fluid, "molten/stone");
    }

}
