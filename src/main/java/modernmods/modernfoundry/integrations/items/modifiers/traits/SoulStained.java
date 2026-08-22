package modernmods.modernfoundry.integrations.items.modifiers.traits;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.EntityHitResult;

import modernmods.hilt.client.TooltipKey;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.tools.TinkerTools;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;

import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class SoulStained extends NoLevelsModifier implements ProjectileHitModifierHook, EquipmentChangeModifierHook, MeleeHitModifierHook, TooltipModifierHook {

    private static final AttributeModifier HELMET_SOUL_WARD_CAP = modifier("helmet_soul_ward_cap", 3.0F);
    private static final AttributeModifier HELMET_SOUL_WARD_RECOVERY = modifier("helmet_soul_ward_recovery", 0.15F);
    private static final AttributeModifier CHESTPLATE_SOUL_WARD_CAP = modifier("chestplate_soul_ward_cap", 3.0F);
    private static final AttributeModifier CHESTPLATE_SOUL_WARD_RECOVERY = modifier("chestplate_soul_ward_recovery", 0.15F);
    private static final AttributeModifier LEGGINGS_SOUL_WARD_CAP = modifier("leggings_soul_ward_cap", 3.0F);
    private static final AttributeModifier LEGGINGS_SOUL_WARD_RECOVERY = modifier("leggings_soul_ward_recovery", 0.15F);
    private static final AttributeModifier BOOTS_SOUL_WARD_CAP = modifier("boots_soul_ward_cap", 3.0F);
    private static final AttributeModifier BOOTS_SOUL_WARD_RECOVERY = modifier("boots_soul_ward_recovery", 0.15F);
    private static final AttributeModifier MELEE_PRIMARY_MAGIC_DAMAGE = modifier("melee_primary_magic_damage", 3);
    private static final AttributeModifier AXE_MAGIC_DAMAGE = modifier("axe_magic_damage", 4);
    private static final AttributeModifier HARVEST_MAGIC_DAMAGE = modifier("harvest_magic_damage", 2);
    private static final AttributeModifier OFFHAND_MELEE_PRIMARY_MAGIC_DAMAGE = modifier("offhand_melee_primary_magic_damage", 3);
    private static final AttributeModifier OFFHAND_AXE_MAGIC_DAMAGE = modifier("offhand_axe_magic_damage", 4);
    private static final AttributeModifier OFFHAND_HARVEST_MAGIC_DAMAGE = modifier("offhand_harvest_magic_damage", 2);
    private static final Component SOUL_WARD_CAPACITY = Component.translatable(
        Util.makeDescriptionId("modifier", resource("soul_stained.soul_ward_capacity")));
    private static final Component SOUL_WARD_RECOVERY_RATE = Component.translatable(
        Util.makeDescriptionId("modifier", resource("soul_stained.soul_ward_recovery_rate")));
    private static final Component PRIMARY_MAGIC_DAMAGE = Component.translatable(
        Util.makeDescriptionId("modifier", resource("soul_stained.primary_magic_damage")));
    private static final Component OFFHAND_MAGIC_DAMAGE = Component.translatable(
        Util.makeDescriptionId("modifier", resource("soul_stained.offhand_magic_damage")));

    private static AttributeModifier modifier(String id, double amount) {
        return new AttributeModifier(resource("soul_stained/" + id), amount, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    protected void registerHooks(@NotNull Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.MELEE_HIT, ModifierHooks.TOOLTIP);
    }

    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            changeEquipment(sp, context, false);
        }
    }

    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            changeEquipment(sp, context, true);
        }
    }

    @Override
    public float beforeMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier,
            ToolAttackContext context, float damage, float baseKnockback, float knockback) {
        applyExposedSoulDuration(context.getLivingTarget());

        return MeleeHitModifierHook.super.beforeMeleeHit(tool, modifier, context, damage, baseKnockback, knockback);
    }

    @Override
    public boolean onProjectileHitEntity(@NotNull ModifierNBT modifiers, @NotNull ModDataNBT persistentData,
            @NotNull ModifierEntry modifier, @NotNull Projectile projectile,
            EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
        if (hit.getEntity() instanceof LivingEntity living) {
            applyExposedSoulDuration(living);
        }

        return false;
    }

    private void applyExposedSoulDuration(LivingEntity entity) {
        if (entity != null) {
            Object data = OptionalIntegrationHelper.invokeStatic(
                "com.sammy.malum.common.capability.MalumLivingEntityDataCapability", "getCapability", entity);
            Object soulData = OptionalIntegrationHelper.field(data, "soulData");
            OptionalIntegrationHelper.setField(soulData, "exposedSoulDuration", 200);
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, @NotNull ModifierEntry modifier, @Nullable Player player,
            @NotNull List<Component> tooltip, @NotNull TooltipKey tooltipKey, @NotNull TooltipFlag tooltipFlag) {
        double soulWardCap = 0.0;
        double soulWardRecoveryRate = 0.0;
        double primaryMagicDamage = 0.0;
        double offhandMagicDamage = 0.0;

        if (tool.hasTag(TinkerTags.Items.HELMETS)) {
            soulWardCap = HELMET_SOUL_WARD_CAP.amount();
            soulWardRecoveryRate = HELMET_SOUL_WARD_RECOVERY.amount();
        }
        else if (tool.hasTag(TinkerTags.Items.CHESTPLATES)) {
            soulWardCap = CHESTPLATE_SOUL_WARD_CAP.amount();
            soulWardRecoveryRate = CHESTPLATE_SOUL_WARD_RECOVERY.amount();
        }
        else if (tool.hasTag(TinkerTags.Items.LEGGINGS)) {
            soulWardCap = LEGGINGS_SOUL_WARD_CAP.amount();
            soulWardRecoveryRate = LEGGINGS_SOUL_WARD_RECOVERY.amount();
        }
        else if (tool.hasTag(TinkerTags.Items.BOOTS)) {
            soulWardCap = BOOTS_SOUL_WARD_CAP.amount();
            soulWardRecoveryRate = BOOTS_SOUL_WARD_RECOVERY.amount();
        }
        else if (tool.hasTag(TinkerTags.Items.MELEE) || tool.hasTag(TinkerTags.Items.HARVEST)) {
            if (tool.getItem().equals(TinkerTools.broadAxe.asItem())) {
                offhandMagicDamage = OFFHAND_AXE_MAGIC_DAMAGE.amount();
                primaryMagicDamage = AXE_MAGIC_DAMAGE.amount();
            }
            else if (tool.hasTag(TinkerTags.Items.MELEE_PRIMARY)) {
                offhandMagicDamage = OFFHAND_MELEE_PRIMARY_MAGIC_DAMAGE.amount();
                primaryMagicDamage = MELEE_PRIMARY_MAGIC_DAMAGE.amount();
            }
            else if (tool.hasTag(TinkerTags.Items.MELEE) || tool.hasTag(TinkerTags.Items.HARVEST)) {
                offhandMagicDamage = OFFHAND_HARVEST_MAGIC_DAMAGE.amount();
                primaryMagicDamage = HARVEST_MAGIC_DAMAGE.amount();
            }
        }

        if (soulWardCap != 0.0) {
            TooltipModifierHook.addFlatBoost(modifier.getModifier(), SOUL_WARD_CAPACITY, soulWardCap, tooltip);
        }
        if (soulWardRecoveryRate != 0.0) {
            TooltipModifierHook.addPercentBoost(modifier.getModifier(), SOUL_WARD_RECOVERY_RATE, soulWardRecoveryRate, tooltip);
        }
        if (primaryMagicDamage != 0.0) {
            TooltipModifierHook.addFlatBoost(modifier.getModifier(), PRIMARY_MAGIC_DAMAGE, primaryMagicDamage, tooltip);
        }
        if (offhandMagicDamage != 0.0) {
            TooltipModifierHook.addFlatBoost(modifier.getModifier(), OFFHAND_MAGIC_DAMAGE, offhandMagicDamage, tooltip);
        }
    }

    public void changeEquipment(ServerPlayer sp, EquipmentChangeContext context, boolean remove) {
        Holder<Attribute> soulWardCapAttribute = OptionalIntegrationHelper.attributeHolder(
            "com.sammy.malum.registry.common.AttributeRegistry", "SOUL_WARD_CAP", "malum:soul_ward_cap");
        Holder<Attribute> soulWardRecoveryAttribute = OptionalIntegrationHelper.attributeHolder(
            "com.sammy.malum.registry.common.AttributeRegistry", "SOUL_WARD_RECOVERY_RATE", "malum:soul_ward_recovery_rate");
        Holder<Attribute> magicDamageAttribute = OptionalIntegrationHelper.attributeHolder(
            "team.lodestar.lodestone.registry.common.LodestoneAttributeRegistry", "MAGIC_DAMAGE", "lodestone:magic_damage");
        final AttributeInstance soulWardCap = soulWardCapAttribute == null ? null : sp.getAttribute(soulWardCapAttribute);
        final AttributeInstance soulWardRecovery = soulWardRecoveryAttribute == null ? null : sp.getAttribute(soulWardRecoveryAttribute);
        final AttributeInstance magicDamage = magicDamageAttribute == null ? null : sp.getAttribute(magicDamageAttribute);
        ItemStack stack;
        AttributeModifier soulWardCapModifier = null;
        AttributeModifier soulWardRecoveryModifier = null;
        AttributeModifier magicDamageModifier = null;
        boolean isArmor = false;
        boolean isTool = false;

        if (remove) {
            stack = context.getOriginal();
        }
        else {
            stack = context.getReplacement();
        }

        switch(context.getChangedSlot()) {
            case FEET -> {
                isArmor = true;
                soulWardCapModifier = BOOTS_SOUL_WARD_CAP;
                soulWardRecoveryModifier = BOOTS_SOUL_WARD_RECOVERY;
            }
            case LEGS -> {
                isArmor = true;
                soulWardCapModifier = LEGGINGS_SOUL_WARD_CAP;
                soulWardRecoveryModifier = LEGGINGS_SOUL_WARD_RECOVERY;
            }
            case CHEST -> {
                isArmor = true;
                soulWardCapModifier = CHESTPLATE_SOUL_WARD_CAP;
                soulWardRecoveryModifier = CHESTPLATE_SOUL_WARD_RECOVERY;
            }
            case HEAD -> {
                isArmor = true;
                soulWardCapModifier = HELMET_SOUL_WARD_CAP;
                soulWardRecoveryModifier = HELMET_SOUL_WARD_RECOVERY;
            }
            case OFFHAND, MAINHAND -> {
                if (stack.is(TinkerTags.Items.MELEE) || stack.is(TinkerTags.Items.HARVEST)) {
                    isTool = true;
                }
            }
        }

        if (isArmor) {
            if (soulWardCap != null) {
                if (remove && soulWardCap.hasModifier(soulWardCapModifier.id())) {
                    soulWardCap.removeModifier(soulWardCapModifier.id());
                }
                else if (!soulWardCap.hasModifier(soulWardCapModifier.id())){
                    soulWardCap.addPermanentModifier(soulWardCapModifier);
                }
            }
            if (soulWardRecovery != null) {
                if (remove && soulWardRecovery.hasModifier(soulWardRecoveryModifier.id())) {
                    soulWardRecovery.removeModifier(soulWardRecoveryModifier.id());
                }
                else if (!soulWardRecovery.hasModifier(soulWardRecoveryModifier.id())){
                    soulWardRecovery.addPermanentModifier(soulWardRecoveryModifier);
                }
            }
        }
        else if (isTool) {
            if (stack.is(TinkerTools.broadAxe.asItem())) {
                if (context.getChangedSlot() == EquipmentSlot.OFFHAND) {
                    magicDamageModifier = OFFHAND_AXE_MAGIC_DAMAGE;
                }
                else {
                    magicDamageModifier = AXE_MAGIC_DAMAGE;
                }
            }
            else if (stack.is(TinkerTags.Items.MELEE_PRIMARY)) {
                if (context.getChangedSlot() == EquipmentSlot.OFFHAND) {
                    magicDamageModifier = OFFHAND_MELEE_PRIMARY_MAGIC_DAMAGE;
                }
                else {
                    magicDamageModifier = MELEE_PRIMARY_MAGIC_DAMAGE;
                }
            }
            else if (stack.is(TinkerTags.Items.MELEE) || stack.is(TinkerTags.Items.HARVEST)) {
                if (context.getChangedSlot() == EquipmentSlot.OFFHAND) {
                    magicDamageModifier = OFFHAND_HARVEST_MAGIC_DAMAGE;
                }
                else {
                    magicDamageModifier = HARVEST_MAGIC_DAMAGE;
                }
            }

            if (magicDamage != null && magicDamageModifier != null) {
                if (remove && magicDamage.hasModifier(magicDamageModifier.id())) {
                    magicDamage.removeModifier(magicDamageModifier.id());
                }
                else if (!magicDamage.hasModifier(magicDamageModifier.id())) {
                    magicDamage.addPermanentModifier(magicDamageModifier);
                }
            }
        }
    }

}
