package modernmods.modernfoundry.integrations.event;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;

import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import modernmods.modernfoundry.integrations.items.TciIntegrationHooks;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.items.modifiers.hooks.IArmorCrouchModifier;
import modernmods.modernfoundry.integrations.network.LaunchGhostSword;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.integrations.util.IfdWorkaroundHelper;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;

public class ToolEventHandler {

    private static final TinkerDataCapability.ComputableDataKey<LastTick> LAST_TICK = createKey("last_tick", LastTick::new);

    @SubscribeEvent
    static void onLivingUpdate(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        if (!living.isSpectator() && !living.level().isClientSide() && living.isAlive()) {
            ItemStack helmet = living.getItemBySlot(EquipmentSlot.HEAD);

            if (!helmet.isEmpty() && helmet.is(TinkerTags.Items.HELMETS)) {
                ToolStack tool = ToolStack.from(helmet);

                for (ModifierEntry entry : tool.getModifierList()) {
                    IArmorCrouchModifier crouchModifier = entry.getHook(TciIntegrationHooks.CROUCH);

                    if (living.isCrouching() || living.isVisuallySwimming()) {
                        crouchModifier.onCrouch(tool, entry.getLevel(), living);
                    }
                    else {
                        crouchModifier.onStand(living);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity living = event.getEntity();

        if (!living.isSpectator() && !living.level().isClientSide() && living.isAlive()) {
            ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);

            if (!boots.isEmpty() && boots.is(TinkerTags.Items.BOOTS)) {
                ToolStack tool = ToolStack.from(boots);

                for (ModifierEntry entry : tool.getModifierList()) {
                    entry.getHook(TciIntegrationHooks.JUMP).onJump(tool, living);
                }
            }
        }
    }


    @SubscribeEvent
    static void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        final Player player = event.getEntity() != null ? event.getEntity() : null;

        if (player != null) {
            if (!TinkerDataCapability.getData(player).computeIfAbsent(LAST_TICK).update(player)) {
                return;
            }

            ItemStack stack = event.getItemStack();
            if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                return;
            }

            ToolStack tool = ToolStack.from(stack);
            List<ModifierEntry> modifiers = tool.getModifierList();

            modifiers.forEach(modifierEntry -> {
                if (IfdWorkaroundHelper.isLoaded() && modifierEntry.getId().equals(TciModifiers.PHANTASMAL_MODIFIER.getId())) {
                    player.playSound(SoundEvents.ZOMBIE_INFECT, 1, 1);

                    TinkerNetwork.getInstance().sendToServer(new LaunchGhostSword());
                }
            });
        }
    }

    private static class LastTick {
        private long lastTick = 0;

        private boolean update(Player player) {
            if (player.tickCount >= lastTick + 4) {
                lastTick = player.tickCount;

                return true;
            }

            return false;
        }
    }

    private static <T> TinkerDataCapability.ComputableDataKey<T> createKey(String name, Supplier<T> constructor) {
        return TinkerDataCapability.ComputableDataKey.of(resource(name), constructor);
    }

}
