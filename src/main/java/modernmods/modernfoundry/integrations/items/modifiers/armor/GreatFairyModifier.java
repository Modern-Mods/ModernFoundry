package modernmods.modernfoundry.integrations.items.modifiers.armor;

import org.jetbrains.annotations.NotNull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.network.BotaniaSetData;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.integrations.util.BotaniaClientHelper;
import modernmods.modernfoundry.integrations.util.BotaniaHelper;

public class GreatFairyModifier extends Modifier implements InventoryTickModifierHook, EquipmentChangeModifierHook, OnAttackedModifierHook {

    private static final int MANA_PER_DAMAGE = 70;

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.ON_ATTACKED);
    }

    public int getManaPerDamage(Player player) {
        return BotaniaHelper.getManaPerDamageBonus(player, MANA_PER_DAMAGE);
    }

    @Override
    public @NotNull Component getDisplayName(int level) {
        return applyStyle(Component.translatable(getTranslationKey()));
    }

    @Override
    public MutableComponent applyStyle(MutableComponent component) {
        if (BotaniaClientHelper.hasGreatFairyArmorSet()) {
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

                data.setGreatFairy(hasSet);

                TinkerNetwork.getInstance().sendTo(new BotaniaSetData(data.hasTerrestrial(), hasSet, data.hasAlfheim()), sp);
            });
        }
    }

    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        final Player player = context.getEntity() instanceof Player ? (Player) context.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            CapabilityRegistry.getBotania(sp).ifPresent(data -> {
                data.setGreatFairy(false);

                TinkerNetwork.getInstance().sendTo(new BotaniaSetData(data.hasTerrestrial(), false, data.hasAlfheim()), sp);
            });
        }
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        // Heal armor if damaged and has mana source
        if (!world.isClientSide
                && holder.tickCount % 20 == 0
                && holder instanceof Player player
                && tool.getDamage() > 0
                && BotaniaHelper.requestManaExactForTool(stack, player, getManaPerDamage(player) * 2, true)) {
            tool.setDamage(tool.getDamage() - 1);
        }
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (context.getEntity() instanceof Player player
                && !player.level().isClientSide
                && source.getEntity() instanceof LivingEntity attacker
                && isDirectDamage
                && BotaniaHelper.hasGreatFairyArmorSet(player)) {
            final ServerPlayer sp = (ServerPlayer) player;
            final Float chance = switch(slotType) {
                case HEAD -> 0.11F;
                case CHEST -> 0.17F;
                case LEGS -> 0.15F;
                default -> 0.09F;
            };

            if (TConstruct.RANDOM.nextFloat() <= chance) {
                BotaniaHelper.spawnPixie(sp, ItemStack.EMPTY, attacker);
            }
        }
    }

    public boolean hasArmorSetItem(ServerPlayer sp, EquipmentSlot slot) {
        ItemStack stack = sp.getItemBySlot(slot);

        if (stack.isEmpty()) return false;

        ToolStack armor = ToolStack.from(stack);

        if (armor.isBroken()) return false;

        return armor.getUpgrades().getLevel(TciModifiers.GREAT_FAIRY_MODIFIER.getId()) > 0;
    }

}
