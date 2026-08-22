package modernmods.modernfoundry.integrations.data.tcon.material;

import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.crafting.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.conditions.ModLoadedCondition;

import modernmods.modernfoundry.library.data.material.AbstractMaterialDataProvider;

import modernmods.modernfoundry.integrations.data.integration.ModIntegration;

public class MaterialDataProvider extends AbstractMaterialDataProvider {

    public MaterialDataProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "TciIntegration - TCon Materials";
    }

    @Override
    protected void addMaterials() {
        ICondition botaniaLoadedCondition = new ModLoadedCondition(ModIntegration.BOTANIA_MODID);

        addMaterial(MaterialIds.livingWood, 1, ORDER_GENERAL, true, false, botaniaLoadedCondition);
        addMaterial(MaterialIds.livingRock, 1, ORDER_GENERAL, true, false, botaniaLoadedCondition);
        addCompatMetalMaterial(MaterialIds.desh, 2, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.calorite, 2, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.ostrum, 2, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.manaSteel, 3, ORDER_COMPAT + ORDER_GENERAL);
        addMaterial(MaterialIds.manaString, 3, ORDER_COMPAT + ORDER_GENERAL, true, false, botaniaLoadedCondition);
        addCompatMetalMaterial(MaterialIds.neptunium, 3, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.soulStainedSteel, 3, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.brass, 3, ORDER_COMPAT + ORDER_REPAIR);
        addCompatMetalMaterial(MaterialIds.pendoriteAlloy, 4, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.dragonsteelFire, 4, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.dragonsteelIce, 4, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(MaterialIds.dragonsteelLightning, 4, ORDER_COMPAT + ORDER_GENERAL);
    }

}
