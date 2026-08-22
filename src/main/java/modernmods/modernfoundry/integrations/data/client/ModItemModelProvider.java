package modernmods.modernfoundry.integrations.data.client;

import modernmods.modernfoundry.TConstruct;

import java.util.Objects;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, TConstruct.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "TciIntegration - Item Models";
    }

    @Override
    protected void registerModels() {
        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));
        ModelFile itemHandheld = getExistingFile(mcLoc("item/handheld"));
    }

    private ItemModelBuilder builder(ModelFile itemGenerated, Item item) {
        String name = Objects.requireNonNull(item.toString());

        return getBuilder(name).parent(itemGenerated).texture("layer0", "item/" + name);
    }

}
