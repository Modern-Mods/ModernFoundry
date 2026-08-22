package modernmods.modernfoundry.integrations.data.tcon;

import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

import modernmods.modernfoundry.library.data.tinkering.AbstractModifierTagProvider;

import modernmods.modernfoundry.integrations.data.tcon.material.TciModifierIds;
import modernmods.modernfoundry.integrations.items.TciModifiers;

import static modernmods.modernfoundry.common.TinkerTags.Modifiers.BOOT_UPGRADES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.CHESTPLATE_UPGRADES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.GENERAL_ABILITIES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.GENERAL_ARMOR_UPGRADES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.GENERAL_UPGRADES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.HARVEST_UPGRADES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.HELMET_UPGRADES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.INTERACTION_ABILITIES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.MELEE_UPGRADES;
import static modernmods.modernfoundry.common.TinkerTags.Modifiers.RANGED_UPGRADES;

public class ModifierTagProvider extends AbstractModifierTagProvider {

    public ModifierTagProvider(PackOutput packOutput, String modId, ExistingFileHelper existingFileHelper) {
        super(packOutput, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(GENERAL_UPGRADES)
            .addOptional(TciModifierIds.livingwood)
            .addOptional(TciModifierIds.engineersGoggles)
            .addOptional(TciModifierIds.multiVision)
            .addOptional(TciModifiers.ALF_MODIFIER.getId())
            .addOptional(TciModifiers.ALFHEIM_MODIFIER.getId())
            .addOptional(TciModifiers.TERRESTRIAL_MODIFIER.getId());
        this.tag(MELEE_UPGRADES)
            .addOptional(TciModifiers.TERRA_MODIFIER.getId())
            .addOptional(TciModifiers.ELEMENTAL_MODIFIER.getId())
            .addOptional(TciModifiers.SIREN_MODIFIER.getId())
            .addOptional(TciModifiers.UTHERIUM_MODIFIER.getId())
            .addOptional(TciModifiers.FLAMED_MODIFIER.getId())
            .addOptional(TciModifiers.ICED_MODIFIER.getId())
            .addOptional(TciModifiers.ZAPPED_MODIFIER.getId());
        this.tag(HARVEST_UPGRADES)
            .addOptional(TciModifiers.SIREN_MODIFIER.getId())
            .addOptional(TciModifiers.FROSTSTEEL_MODIFIER.getId())
            .addOptional(TciModifiers.FORGOTTEN_MODIFIER.getId());
        this.tag(GENERAL_ARMOR_UPGRADES)
            .addOptional(TciModifiers.GREAT_FAIRY_MODIFIER.getId())
            .addOptional(TciModifiers.ALFHEIM_MODIFIER.getId())
            .addOptional(TciModifiers.ARS_MODIFIER.getId())
            .addOptional(TciModifiers.SOUL_STAINED_MODIFIER.getId())
            .addOptional(TciModifierIds.masticate);
        this.tag(GENERAL_ABILITIES)
            .addOptional(TciModifiers.MECHANICAL_ARM_MODIFIER.getId())
            .addOptional(TciModifiers.POSEIDON_MODIFIER.getId())
            .addOptional(TciModifiers.ALFHEIM_MODIFIER.getId());
        this.tag(INTERACTION_ABILITIES)
            .addOptional(TciModifiers.ALF_MODIFIER.getId())
            .addOptional(TciModifiers.GLOWUP_MODIFIER.getId());
        this.tag(HELMET_UPGRADES)
            .addOptional(TciModifiers.FRONTIER_CAP_MODIFIER.getId())
            .addOptional(TciModifiers.TURTLE_SHELL_MODIFIER.getId())
            .addOptional(TciModifiers.BISON_FUR_MODIFIER.getId());
        this.tag(CHESTPLATE_UPGRADES)
            .addOptional(TciModifiers.ENCHANTERS_SHIELD_MODIFIER.getId())
            .addOptional(TciModifiers.SHIELD_OF_THE_DEEP_MODIFIER.getId())
            .addOptional(TciModifiers.CROCODILE_MODIFIER.getId());
        this.tag(BOOT_UPGRADES)
            .addOptional(TciModifiers.ROADRUNNER_MODIFIER.getId())
            .addOptional(TciModifiers.MOSQUITO_MODIFIER.getId());
        this.tag(RANGED_UPGRADES)
            .addOptional(TciModifiers.FLAMED_MODIFIER.getId())
            .addOptional(TciModifiers.ICED_MODIFIER.getId())
            .addOptional(TciModifiers.ZAPPED_MODIFIER.getId());
    }

    @Override
    public String getName() {
        return "TciIntegration - TCon Modifier Tag Provider";
    }

}
