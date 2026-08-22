package modernmods.modernfoundry.integrations.items.modifiers.armor;

import org.jetbrains.annotations.NotNull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.NeoForgeMod;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;
import modernmods.modernfoundry.integrations.items.modifiers.hooks.IArmorJumpModifier;
import modernmods.modernfoundry.integrations.items.TciIntegrationHooks;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.network.BotaniaSetData;
import modernmods.modernfoundry.integrations.util.BotaniaClientHelper;
import modernmods.modernfoundry.integrations.util.BotaniaHelper;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class AlfheimModifier extends Modifier implements IArmorJumpModifier, EquipmentChangeModifierHook, InventoryTickModifierHook {

    private static final int MANA_PER_DAMAGE = 110;

    private static final AttributeModifier HELMET_REACH_DISTANCE = new AttributeModifier(
            TConstruct.getResource("alfheim/helmet_reach"),
            OptionalIntegrationHelper.configNumber("mythicbotany.config.MythicConfig", "alftools", "reach_modifier", 0.0),
            AttributeModifier.Operation.ADD_VALUE
    );

    private static final AttributeModifier CHESTPLATE_KNOCKBACK_RESISTANCE = new AttributeModifier(
            TConstruct.getResource("alfheim/chestplate_knockback_resistance"),
            OptionalIntegrationHelper.configNumber("mythicbotany.config.MythicConfig", "alftools", "knockback_resistance_modifier", 0.0),
            AttributeModifier.Operation.ADD_VALUE
    );

    private static final AttributeModifier LEGGINGS_MOVEMENT_SPEED = new AttributeModifier(
            TConstruct.getResource("alfheim/leggings_movement_speed"),
            OptionalIntegrationHelper.configNumber("mythicbotany.config.MythicConfig", "alftools", "speed_modifier", 0.0),
            AttributeModifier.Operation.ADD_VALUE
    );

    private static final AttributeModifier LEGGINGS_SWIM_SPEED = new AttributeModifier(
            TConstruct.getResource("alfheim/leggings_swim_speed"),
            OptionalIntegrationHelper.configNumber("mythicbotany.config.MythicConfig", "alftools", "speed_modifier", 0.0),
            AttributeModifier.Operation.ADD_VALUE
    );

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, TciIntegrationHooks.JUMP, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.INVENTORY_TICK);
    }

    public int getManaPerDamage(ServerPlayer sp) {
        return BotaniaHelper.getManaPerDamageBonus(sp, MANA_PER_DAMAGE);
    }

    @Override
    public @NotNull Component getDisplayName(int level) {
        return applyStyle(Component.translatable(getTranslationKey()));
    }

    @Override
    public MutableComponent applyStyle(MutableComponent component) {
        if (BotaniaClientHelper.hasAlfheimArmorSet()) {
            return component.withStyle(style -> style.withColor(getTextColor()));
        }
        else {
            return component.withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.STRIKETHROUGH);
        }
    }

    @Override
    public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            CapabilityRegistry.getBotania(sp).ifPresent(data -> {
                boolean hasSet =
                        hasArmorSetItem(sp, EquipmentSlot.HEAD) &&
                        hasArmorSetItem(sp, EquipmentSlot.CHEST) &&
                        hasArmorSetItem(sp, EquipmentSlot.LEGS) &&
                        hasArmorSetItem(sp, EquipmentSlot.FEET);

                data.setAlfheim(hasSet);

                TinkerNetwork.getInstance().sendTo(new BotaniaSetData(data.hasTerrestrial(), data.hasGreatFairy(), hasSet), sp);
            });

            changeEquipment(sp, context, false);
        }
    }

    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            CapabilityRegistry.getBotania(sp).ifPresent(data -> {
                data.setTerrestrial(false);

                TinkerNetwork.getInstance().sendTo(new BotaniaSetData(data.hasTerrestrial(), data.hasGreatFairy(), false), sp);
            });

            changeEquipment(sp, context, true);
        }
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        final Player player = holder instanceof Player ? (Player) holder : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            // Heal armor if damaged and has mana source
            if (sp.tickCount % 20 == 0
                    && tool.getDamage() > 0
                    && BotaniaHelper.requestManaExactForTool(stack, sp, getManaPerDamage(sp), true)) {
                tool.setDamage(tool.getDamage() - 1);
            }
        }
    }

    public boolean hasArmorSetItem(ServerPlayer sp, EquipmentSlot slot) {
        ItemStack stack = sp.getItemBySlot(slot);

        if (stack.isEmpty()) return false;

        ToolStack armor = ToolStack.from(stack);

        if (armor.isBroken()) return false;

        return armor.getUpgrades().getLevel(TciModifiers.ALFHEIM_MODIFIER.getId()) > 0;
    }

    public void changeEquipment(ServerPlayer sp, EquipmentChangeContext context, boolean remove) {
        final AttributeInstance reachDistance = sp.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        final AttributeInstance knockbackResistance = sp.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        final AttributeInstance movementSpeed = sp.getAttribute(Attributes.MOVEMENT_SPEED);
        final AttributeInstance swimSpeed = sp.getAttribute(NeoForgeMod.SWIM_SPEED);

        if (context.getChangedSlot() == EquipmentSlot.LEGS && movementSpeed != null) {
            if (remove && movementSpeed.hasModifier(LEGGINGS_MOVEMENT_SPEED.id())) {
                movementSpeed.removeModifier(LEGGINGS_MOVEMENT_SPEED.id());
            }
            else if (!movementSpeed.hasModifier(LEGGINGS_MOVEMENT_SPEED.id())) {
                movementSpeed.addPermanentModifier(LEGGINGS_MOVEMENT_SPEED);
            }
        }

        if (context.getChangedSlot() == EquipmentSlot.LEGS && swimSpeed != null) {
            if (remove && swimSpeed.hasModifier(LEGGINGS_SWIM_SPEED.id())) {
                swimSpeed.removeModifier(LEGGINGS_SWIM_SPEED.id());
            }
            else if (!swimSpeed.hasModifier(LEGGINGS_SWIM_SPEED.id())) {
                swimSpeed.addPermanentModifier(LEGGINGS_SWIM_SPEED);
            }
        }

        if (context.getChangedSlot() == EquipmentSlot.CHEST && knockbackResistance != null) {
            if (remove && knockbackResistance.hasModifier(CHESTPLATE_KNOCKBACK_RESISTANCE.id())) {
                knockbackResistance.removeModifier(CHESTPLATE_KNOCKBACK_RESISTANCE.id());
            }
            else if (!knockbackResistance.hasModifier(CHESTPLATE_KNOCKBACK_RESISTANCE.id())) {
                knockbackResistance.addPermanentModifier(CHESTPLATE_KNOCKBACK_RESISTANCE);
            }
        }
        if (context.getChangedSlot() == EquipmentSlot.HEAD && reachDistance != null) {
            if (remove && reachDistance.hasModifier(HELMET_REACH_DISTANCE.id())) {
                reachDistance.removeModifier(HELMET_REACH_DISTANCE.id());
            }
            else if (!reachDistance.hasModifier(HELMET_REACH_DISTANCE.id())) {
                reachDistance.addPermanentModifier(HELMET_REACH_DISTANCE);
            }
        }
    }

    @Override
    public void onJump(IToolStackView tool, LivingEntity living) {
        float rot = living.getYRot() * ((float)Math.PI / 180F);
        float jumpModifier = (float) OptionalIntegrationHelper.configNumber("mythicbotany.config.MythicConfig", "alftools", "jump_modifier", 0.0);
        float xzFactor = living.isSprinting() ? jumpModifier : 0;

        living.setDeltaMovement(living.getDeltaMovement().add(-Mth.sin(rot) * xzFactor, jumpModifier, Mth.cos(rot) * xzFactor));
    }

}
