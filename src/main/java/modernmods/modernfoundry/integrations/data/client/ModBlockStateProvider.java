package modernmods.modernfoundry.integrations.data.client;

import modernmods.modernfoundry.TConstruct;

import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, TConstruct.MOD_ID, exFileHelper);
    }

    @Override
    public String getName() {
        return "TciIntegration - Block State and Models";
    }

    @Override
    protected void registerStatesAndModels() {
        generateBronzeModels();
    }

    private void generateBronzeModels() {
    }

}
