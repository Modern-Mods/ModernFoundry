package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;

import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import modernmods.modernfoundry.integrations.common.capabilities.ArsElementalSet;
import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.network.ArsElementalSetData;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class ArsElementalSetBase extends Modifier implements EquipmentChangeModifierHook, ModifyDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.MODIFY_DAMAGE);
    }

    @Override
    public Component getDisplayName(int level) {
        return applyStyle(Component.translatable(getTranslationKey()));
    }

    public boolean hasArmorSet() {
        return false;
    }

    public MutableComponent applyStyle(MutableComponent component) {
        if (hasArmorSet()) {
            return component.withStyle(style -> style.withColor(getTextColor()));
        }
        else {
            return component.withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.STRIKETHROUGH);
        }
    }

    public void setHasSet(ArsElementalSet data, boolean hasSet) {}

    @Override
    public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            ItemStack replacement = context.getReplacement();

            CapabilityRegistry.getArsElemental(sp).ifPresent(data -> {
                boolean hasSet =
                        hasArmorSetItem(sp, EquipmentSlot.HEAD) &&
                                hasArmorSetItem(sp, EquipmentSlot.CHEST) &&
                                hasArmorSetItem(sp, EquipmentSlot.LEGS) &&
                                hasArmorSetItem(sp, EquipmentSlot.FEET);

                setHasSet(data, hasSet);

                TinkerNetwork.getInstance().sendTo(new ArsElementalSetData(data.hasAir(), data.hasAqua(), data.hasEarth(), data.hasFire()), sp);
            });

            int modifierLevel = tool.getModifierLevel(TciModifiers.ARS_MODIFIER.get());

            modifierLevel++;

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

    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            CapabilityRegistry.getArsElemental(sp).ifPresent(data -> {
                setHasSet(data, false);

                TinkerNetwork.getInstance().sendTo(new ArsElementalSetData(data.hasAir(), data.hasAqua(), data.hasEarth(), data.hasFire()), sp);
            });
        }
    }

    public ModifierId getModifierId() {
        return TciModifiers.AETHERMANCER_MODIFIER.getId();
    }

    public boolean hasArmorSetItem(ServerPlayer sp, EquipmentSlot slot) {
        ItemStack stack = sp.getItemBySlot(slot);

        if (stack.isEmpty()) return false;

        ToolStack armor = ToolStack.from(stack);

        if (armor.isBroken()) return false;

        return armor.getUpgrades().getLevel(getModifierId()) > 0;
    }

    public boolean hasArmorSet(Player player) {
        return false;
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slot, DamageSource damageSource, float amount, boolean isDirectDamage) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;
        int bonusReduction = 0;

        if (player != null && !player.level().isClientSide) {
            if (hasArmorSet(player) && damageSource.is(DamageTypeTags.IS_FALL)) {
                bonusReduction += 5;
            }
            else if (hasArmorSet(player) && damageSource.is(DamageTypeTags.IS_DROWNING)) {
                player.setAirSupply(player.getMaxAirSupply());
                bonusReduction += 5;
            }
            else if (hasArmorSet(player) && damageSource.is(DamageTypeTags.IS_FIRE)) {
                player.clearFire();
                bonusReduction += 5;
            }
        }

        if (bonusReduction > 0) {
            int finalBonusReduction = bonusReduction;

            Object mana = OptionalIntegrationHelper.capability(player,
                    "com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry", "MANA_CAPABILITY");
            if (mana != null) {
                OptionalIntegrationHelper.invoke(mana, "addMana", amount);
                Holder<MobEffect> manaRegen = OptionalIntegrationHelper.effectHolder(
                        "com.hollingsworth.arsnouveau.setup.registry.ModPotions", "MANA_REGEN_EFFECT", "ars_nouveau:mana_regen");
                if (manaRegen != null) player.addEffect(new MobEffectInstance(manaRegen, 200, finalBonusReduction / 2));
            }

            return amount * (1 - (bonusReduction / 10F));
        }
        else {
            return amount;
        }
    }

}
