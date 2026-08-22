package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.common.NeoForgeMod;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;
import modernmods.modernfoundry.TConstruct;

public class CrocodileModifier extends NoLevelsModifier implements EquipmentChangeModifierHook {

    private static final AttributeModifier INCREASED_SWIM_SPEED = new AttributeModifier(
        TConstruct.getResource("crocodile/swim_speed"),
        1,
        AttributeModifier.Operation.ADD_VALUE
    );
    private static final AttributeModifier INCREASED_ARMOR = new AttributeModifier(
        TConstruct.getResource("crocodile/armor"),
        OptionalIntegrationHelper.number(OptionalIntegrationHelper.invoke(
            OptionalIntegrationHelper.unwrap(OptionalIntegrationHelper.staticField(
                "com.github.alexthe666.alexsmobs.item.AMItemRegistry", "CROCODILE_ARMOR_MATERIAL")),
            "getDefenseForType", ArmorItem.Type.CHESTPLATE), 0),
        AttributeModifier.Operation.ADD_VALUE
    );
    private static final AttributeModifier INCREASED_ARMOR_TOUGHNESS = new AttributeModifier(
        TConstruct.getResource("crocodile/armor_toughness"),
        OptionalIntegrationHelper.number(OptionalIntegrationHelper.invoke(
            OptionalIntegrationHelper.unwrap(OptionalIntegrationHelper.staticField(
                "com.github.alexthe666.alexsmobs.item.AMItemRegistry", "CROCODILE_ARMOR_MATERIAL")),
            "getToughness"), 0),
        AttributeModifier.Operation.ADD_VALUE
    );

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
    }

    @Override
    public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            AttributeInstance swimSpeed = sp.getAttribute(NeoForgeMod.SWIM_SPEED);
            AttributeInstance armor = sp.getAttribute(Attributes.ARMOR);
            AttributeInstance armorToughness = sp.getAttribute(Attributes.ARMOR_TOUGHNESS);

            if (swimSpeed != null
                    && !swimSpeed.hasModifier(INCREASED_SWIM_SPEED.id())) {
                swimSpeed.addPermanentModifier(INCREASED_SWIM_SPEED);
            }

            if (armor != null
                    && !armor.hasModifier(INCREASED_ARMOR.id())) {
                armor.addPermanentModifier(INCREASED_ARMOR);
            }

            if (armorToughness != null
                    && !armorToughness.hasModifier(INCREASED_ARMOR_TOUGHNESS.id())) {
                armorToughness.addPermanentModifier(INCREASED_ARMOR_TOUGHNESS);
            }
        }
    }

    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            AttributeInstance swimSpeed = sp.getAttribute(NeoForgeMod.SWIM_SPEED);
            AttributeInstance armor = sp.getAttribute(Attributes.ARMOR);
            AttributeInstance armorToughness = sp.getAttribute(Attributes.ARMOR_TOUGHNESS);

            if (swimSpeed != null
                    && swimSpeed.hasModifier(INCREASED_SWIM_SPEED.id())) {
                swimSpeed.removeModifier(INCREASED_SWIM_SPEED.id());
            }

            if (armor != null
                    && armor.hasModifier(INCREASED_ARMOR.id())) {
                armor.removeModifier(INCREASED_ARMOR.id());
            }

            if (armorToughness != null
                    && armorToughness.hasModifier(INCREASED_ARMOR_TOUGHNESS.id())) {
                armorToughness.removeModifier(INCREASED_ARMOR_TOUGHNESS.id());
            }
        }
    }

}
