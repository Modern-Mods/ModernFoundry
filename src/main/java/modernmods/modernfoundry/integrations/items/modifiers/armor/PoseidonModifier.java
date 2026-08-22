package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.NeoForgeMod;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class PoseidonModifier extends NoLevelsModifier implements InventoryTickModifierHook, EquipmentChangeModifierHook {

    private static final AttributeModifier INCREASED_SWIM_SPEED = new AttributeModifier(
            resource("poseidon/swim_speed"),
            0.5D,
            AttributeModifier.Operation.ADD_VALUE
    );

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.EQUIPMENT_CHANGE);
    }

    @Override
    public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            AttributeInstance swimSpeed = sp.getAttribute(NeoForgeMod.SWIM_SPEED);

            if (context.getChangedSlot() == EquipmentSlot.FEET
                    && swimSpeed != null
                    && !swimSpeed.hasModifier(INCREASED_SWIM_SPEED.id())) {
                swimSpeed.addPermanentModifier(INCREASED_SWIM_SPEED);
            }
        }
    }

    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            AttributeInstance swimSpeed = sp.getAttribute(NeoForgeMod.SWIM_SPEED);

            if (context.getChangedSlot() == EquipmentSlot.FEET
                    && swimSpeed != null
                    && swimSpeed.hasModifier(INCREASED_SWIM_SPEED.id())) {
                swimSpeed.removeModifier(INCREASED_SWIM_SPEED.id());
            }
        }
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        final Player player = holder instanceof Player ? (Player) holder : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            boolean isSubmerged = sp.isUnderWater() && sp.isInWater() && sp.isInWaterRainOrBubble();

            if (isSubmerged) {
                if (itemSlot == EquipmentSlot.HEAD.getIndex()) {
                    // Underwater vision
                    sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, false, false, false));
                }
                else if (itemSlot == EquipmentSlot.CHEST.getIndex()) {
                    // Underwater breathing
                    sp.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, false, false, false));
                }
                else if (itemSlot == EquipmentSlot.LEGS.getIndex() && !sp.isCrouching()) {
                    // Weightless
                    sp.setDeltaMovement(sp.getDeltaMovement().add(0, sp.fallDistance, 0));
                }
            }
        }
    }

}
