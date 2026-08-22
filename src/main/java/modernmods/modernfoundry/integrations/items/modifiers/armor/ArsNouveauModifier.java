package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.util.Lazy;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.items.modifiers.ArsNouveauBaseModifier;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class ArsNouveauModifier extends ArsNouveauBaseModifier implements EquipmentChangeModifierHook {

    private final Lazy<Component> MAGE_NAME = Lazy.of(() -> applyStyle(Component.translatable(getTranslationKey() + ".2")));
    private final Lazy<Component> ARCHMAGE_NAME = Lazy.of(() -> applyStyle(Component.translatable(getTranslationKey() + ".3")));

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
    }

    @Override
    public Component getDisplayName(int level) {
        return switch(level) {
            case 2 -> MAGE_NAME.get();
            case 3 -> ARCHMAGE_NAME.get();
            default -> super.getDisplayName();
        };
    }

    @Override
    public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            ItemStack replacement = context.getReplacement();
            int modifierLevel = tool.getModifierLevel(modifier.getModifier());

            if (ModIntegration.canLoad(ModIntegration.ARS_ELEMENTAL_MODID)) {
                if (tool.getModifierLevel(TciModifiers.AETHERMANCER_MODIFIER.get()) > 0) {
                    modifierLevel++;
                } else if (tool.getModifierLevel(TciModifiers.GEOMANCER_MODIFIER.get()) > 0) {
                    modifierLevel++;
                } else if (tool.getModifierLevel(TciModifiers.AQUAMANCER_MODIFIER.get()) > 0) {
                    modifierLevel++;
                }
            }

            OptionalIntegrationHelper.setEnchantment(replacement,
                    OptionalIntegrationHelper.enchantmentHolder(
                            "com.hollingsworth.arsnouveau.setup.registry.EnchantmentRegistry",
                            "MANA_BOOST_ENCHANTMENT", "ars_nouveau:mana_boost"), modifierLevel);
            OptionalIntegrationHelper.setEnchantment(replacement,
                    OptionalIntegrationHelper.enchantmentHolder(
                            "com.hollingsworth.arsnouveau.setup.registry.EnchantmentRegistry",
                            "MANA_REGEN_ENCHANTMENT", "ars_nouveau:mana_regen"), modifierLevel);
        }
    }

}

