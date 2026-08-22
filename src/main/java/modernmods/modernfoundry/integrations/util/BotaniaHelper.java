package modernmods.modernfoundry.integrations.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;

import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;

public class BotaniaHelper {

    private static final List<Holder<MobEffect>> potions = List.of(
        MobEffects.BLINDNESS,
        MobEffects.WITHER,
        MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.WEAKNESS
    );

    public static void spawnPixie(ServerPlayer sp, ItemStack stack, LivingEntity target) {
        Object created = OptionalIntegrationHelper.newInstance("vazkii.botania.common.entity.PixieEntity", sp.level());
        if (!(created instanceof LivingEntity pixie)) return;

        pixie.setPos(sp.getX(), sp.getY() + 2, sp.getZ());

        if (hasGreatFairyArmorSet(sp)) {
            OptionalIntegrationHelper.invoke(pixie, "setApplyPotionEffect", new MobEffectInstance(potions.get(sp.level().random.nextInt(potions.size())), 40, 0));
        }

        float dmg = 4;

        if (!stack.isEmpty()) {
            dmg += 2;
        }

        OptionalIntegrationHelper.invoke(pixie, "setProps", target, sp, 0, dmg);
        OptionalIntegrationHelper.invoke(pixie, "finalizeSpawn",
                (ServerLevelAccessor) sp.level(),
                sp.level().getCurrentDifficultyAt(pixie.blockPosition()),
                MobSpawnType.EVENT, null, null);
        sp.level().addFreshEntity(pixie);
    }


    public static int getManaPerDamageBonus(Player player, int mana) {
        AtomicReference<Double> decreaseModifier = new AtomicReference<>(1.0);

        var data = CapabilityRegistry.botania(player);
        if (data.hasTerrestrial()) {
            decreaseModifier.set(data.hasAlfheim() ? 0.8 : 0.7);
        } else if (data.hasGreatFairy()) {
            decreaseModifier.set(0.9);
        }

        return (int) (mana * decreaseModifier.get());
    }

    public static boolean dispatchManaExact(ServerPlayer player, int manaToSend) {
        Object handler = manaHandler();
        if (handler == null) return false;

        Object[] sources = {
                OptionalIntegrationHelper.invoke(handler, "getManaItems", player),
                OptionalIntegrationHelper.invoke(handler, "getManaAccesories", player),
                OptionalIntegrationHelper.invoke(handler, "getManaAccessories", player)
        };
        Object abstractions = OptionalIntegrationHelper.staticField("vazkii.botania.xplat.XplatAbstractions", "INSTANCE");

        for (Object source : sources) {
            if (!(source instanceof Iterable<?> iterable)) continue;
            for (Object value : iterable) {
                if (!(value instanceof ItemStack stackInSlot)) continue;
                Object manaItem = OptionalIntegrationHelper.invoke(abstractions, "findManaItem", stackInSlot);
                double current = OptionalIntegrationHelper.number(OptionalIntegrationHelper.invoke(manaItem, "getMana"), -1);
                double maximum = OptionalIntegrationHelper.number(OptionalIntegrationHelper.invoke(manaItem, "getMaxMana"), -1);
                if (current >= 0 && maximum >= current + manaToSend) {
                    OptionalIntegrationHelper.invoke(manaItem, "addMana", manaToSend);
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean requestManaExactForTool(ItemStack stack, Player player, int mana, boolean action) {
        Object handler = manaHandler();
        return OptionalIntegrationHelper.bool(OptionalIntegrationHelper.invoke(handler, "requestManaExactForTool", stack, player, mana, action));
    }

    private static Object manaHandler() {
        Object handler = OptionalIntegrationHelper.staticField("vazkii.botania.api.mana.ManaItemHandler", "INSTANCE");
        return handler != null ? handler : OptionalIntegrationHelper.invokeStatic("vazkii.botania.api.mana.ManaItemHandler", "instance");
    }

    public static boolean hasTerrestrialArmorSet(Player player) {
        return CapabilityRegistry.botania(player).hasTerrestrial();
    }

    public static boolean hasGreatFairyArmorSet(Player player) {
        return CapabilityRegistry.botania(player).hasGreatFairy();
    }

    public static boolean hasAlfheimArmorSet(Player player) {
        return CapabilityRegistry.botania(player).hasAlfheim();
    }

}
