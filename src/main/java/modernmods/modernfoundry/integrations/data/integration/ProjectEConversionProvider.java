package modernmods.modernfoundry.integrations.data.integration;

import modernmods.modernfoundry.TConstruct;

import java.util.concurrent.CompletableFuture;

import moze_intel.projecte.api.data.CustomConversionProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import modernmods.modernfoundry.integrations.shared.TinkerMaterials;


import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.location;

public class ProjectEConversionProvider extends CustomConversionProvider {

    public ProjectEConversionProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "TciIntegration - ProjectE Conversion Provider";
    }

    @Override
    protected void addCustomConversions(HolderLookup.Provider provider) {
        createConversionBuilder(location(TConstruct.MOD_ID, "metals"))
            .before(TinkerMaterials.cobalt.getIngot(), 6_144);
    }

}
