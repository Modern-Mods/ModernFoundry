package modernmods.modernfoundry.integrations.items.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class ArsNouveauBaseModifier extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        final Player player = holder instanceof Player ? (Player) holder : null;

        if (player != null
                && !player.level().isClientSide
                && holder.tickCount % 200 == 0
                && holder.getUseItem() != stack
                && tool.getDamage() > 0) {
            final ServerPlayer sp = (ServerPlayer) player;

            Object mana = OptionalIntegrationHelper.capability(sp,
                    "com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry", "MANA_CAPABILITY");
            if (OptionalIntegrationHelper.number(OptionalIntegrationHelper.invoke(mana, "getCurrentMana"), 0) > 20) {
                OptionalIntegrationHelper.invoke(mana, "removeMana", 20);
                tool.setDamage(tool.getDamage() - 1);
            }
        }
    }

}

