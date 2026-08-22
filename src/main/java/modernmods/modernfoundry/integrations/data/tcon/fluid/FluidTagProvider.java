package modernmods.modernfoundry.integrations.data.tcon.fluid;

import modernmods.modernfoundry.TConstruct;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

import modernmods.hilt.registration.object.FlowingFluidObject;
import modernmods.hilt.registration.object.FluidObject;

import modernmods.modernfoundry.common.TinkerTags;

import modernmods.modernfoundry.integrations.items.TciItems;

@SuppressWarnings("unchecked")
public class FluidTagProvider extends FluidTagsProvider {

    public FluidTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper helper) {
        super(packOutput, lookupProvider, TConstruct.MOD_ID, helper);
    }

    @Override
    public String getName() {
        return "TciIntegration - TCon Fluid Tags";
    }

    @Override
    public void addTags(HolderLookup.@NotNull Provider provider) {
        fluidTag(TciItems.MOLTEN_MANASTEEL);
        fluidTag(TciItems.MOLTEN_NEPTUNIUM);
        fluidTag(TciItems.MOLTEN_SOURCE_GEM);
        fluidTag(TciItems.MOLTEN_SOUL_STAINED_STEEL);
        fluidTag(TciItems.MOLTEN_CLOGGRUM);
        fluidTag(TciItems.MOLTEN_FROSTSTEEL);
        fluidTag(TciItems.MOLTEN_FORGOTTEN_METAL);
        fluidTag(TciItems.MOLTEN_DESH);
        fluidTag(TciItems.MOLTEN_CALORITE);
        fluidTag(TciItems.MOLTEN_OSTRUM);
        fluidTag(TciItems.MOLTEN_DRAGONSTEEL_FIRE);
        fluidTag(TciItems.MOLTEN_DRAGONSTEEL_ICE);
        fluidTag(TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING);

        this.tag(TinkerTags.Fluids.METAL_TOOLTIPS)
            .addOptionalTag(TciItems.MOLTEN_MANASTEEL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_NEPTUNIUM.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_SOUL_STAINED_STEEL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_CLOGGRUM.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_FROSTSTEEL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_FORGOTTEN_METAL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DESH.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_CALORITE.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_OSTRUM.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_FIRE.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_ICE.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING.getTag().location());

        this.tag(TinkerTags.Fluids.AVERAGE_METAL_SPILLING)
            .addOptionalTag(TciItems.MOLTEN_MANASTEEL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_SOUL_STAINED_STEEL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_CLOGGRUM.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_FROSTSTEEL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_FIRE.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_ICE.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING.getTag().location());

        this.tag(TinkerTags.Fluids.EXPENSIVE_METAL_SPILLING)
            .addOptionalTag(TciItems.MOLTEN_NEPTUNIUM.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_FORGOTTEN_METAL.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_FIRE.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_ICE.getTag().location())
            .addOptionalTag(TciItems.MOLTEN_DRAGONSTEEL_LIGHTNING.getTag().location());

        this.tag(TinkerTags.Fluids.SMALL_GEM_TOOLTIPS)
            .addOptionalTag(TciItems.MOLTEN_SOURCE_GEM.getId());
    }

    private void fluidTag(FluidObject<?> fluid) {
        tag(Objects.requireNonNull(fluid.getCommonTag())).add(fluid.get());
    }

    /** Adds tags for a placable fluid */
    private void fluidTag(FlowingFluidObject<?> fluid) {
        tag(fluid.getLocalTag()).add(fluid.getStill(), fluid.getFlowing());
        TagKey<Fluid> tag = fluid.getCommonTag();

        if (tag != null) {
            tag(tag).addTag(fluid.getLocalTag());
        }
    }

}
