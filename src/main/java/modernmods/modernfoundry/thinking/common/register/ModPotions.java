package modernmods.modernfoundry.thinking.common.register;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import modernmods.hilt.registration.deferred.PotionDeferredRegister;

public class ModPotions{
    private static final PotionDeferredRegister POTIONS = new PotionDeferredRegister(TConstruct.MOD_ID);
    public static final DeferredHolder<Potion, Potion> overweight_potion = POTIONS.register("overweight_potion", () -> new Potion(new MobEffectInstance(ModEffects.overweight, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> overweight_potion_strong = POTIONS.register("overweight_potion_strong", () -> new Potion(new MobEffectInstance(ModEffects.overweight, 400, 1)));
    public static final DeferredHolder<Potion, Potion> overweight_potion_long = POTIONS.register("overweight_potion_long", () -> new Potion(new MobEffectInstance(ModEffects.overweight, 2400, 0)));
    public static final DeferredHolder<Potion, Potion> weightless_potion = POTIONS.register("weightless_potion", () -> new Potion(new MobEffectInstance(ModEffects.weightless, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> weightless_potion_strong = POTIONS.register("weightless_potion_strong", () -> new Potion(new MobEffectInstance(ModEffects.weightless, 400, 1)));
    public static final DeferredHolder<Potion, Potion> weightless_potion_long = POTIONS.register("weightless_potion_long", () -> new Potion(new MobEffectInstance(ModEffects.weightless, 2400, 0)));

    @SubscribeEvent
    void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, ModCommonItems.clay_crystal.get(), holder(overweight_potion));
        event.getBuilder().addMix(holder(overweight_potion), Items.GLOWSTONE_DUST, holder(overweight_potion_strong));
        event.getBuilder().addMix(holder(overweight_potion), Items.REDSTONE, holder(overweight_potion_long));
        event.getBuilder().addMix(holder(overweight_potion), Items.FERMENTED_SPIDER_EYE, holder(weightless_potion));
        event.getBuilder().addMix(holder(overweight_potion_strong), Items.FERMENTED_SPIDER_EYE, holder(weightless_potion_strong));
        event.getBuilder().addMix(holder(overweight_potion_long), Items.FERMENTED_SPIDER_EYE, holder(weightless_potion_long));
        event.getBuilder().addMix(holder(weightless_potion), Items.GLOWSTONE_DUST, holder(weightless_potion_strong));
        event.getBuilder().addMix(holder(weightless_potion), Items.REDSTONE, holder(weightless_potion_long));
    }

    private static Holder<Potion> holder(DeferredHolder<Potion, Potion> potion) {
        return BuiltInRegistries.POTION.wrapAsHolder(potion.get());
    }

    public static void registers(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
