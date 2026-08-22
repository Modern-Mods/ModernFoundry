package modernmods.modernfoundry.integrations.data.tcon;

import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.crafting.conditions.IConditionBuilder;

import modernmods.modernfoundry.library.data.tinkering.AbstractModifierProvider;
import modernmods.modernfoundry.library.modifiers.modules.behavior.RepairModule;
import modernmods.modernfoundry.library.modifiers.modules.build.ModifierSlotModule;
import modernmods.modernfoundry.library.modifiers.util.ModifierLevelDisplay;
import modernmods.modernfoundry.library.tools.SlotType;

import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.data.tcon.material.TciModifierIds;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.items.modifiers.armor.EngineersGogglesModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.MultiVisionModifier;

public class ModifierProvider extends AbstractModifierProvider implements IConditionBuilder {

    public ModifierProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "TciIntegration - TCon Modifiers";
    }

    @Override
    protected void addModifiers() {
        buildModifier(TciModifierIds.livingwood)
            .addModule(RepairModule.builder().eachLevel(0.75F));
        buildModifier(TciModifierIds.engineersGoggles, modLoaded(ModIntegration.CREATE_MODID))
            .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
            .addModule(EngineersGogglesModifier.INSTANCE);
        buildModifier(TciModifiers.ARS_MODIFIER.getId())
            .levelDisplay(new ModifierLevelDisplay.UniqueForLevels(3))
            .addModule(new ModifierSlotModule(SlotType.UPGRADE));
        buildModifier(TciModifierIds.multiVision, modLoaded(ModIntegration.IE_MODID))
            .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
            .addModule(MultiVisionModifier.INSTANCE);
    }

}
