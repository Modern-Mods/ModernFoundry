package modernmods.modernfoundry.thinking.common.library;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.register.ModCommonItems;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import modernmods.modernfoundry.thinking.common.register.ModToolItems;
import modernmods.modernfoundry.thinking.data.ModDamageTypes;
import modernmods.modernfoundry.thinking.data.ModTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import modernmods.modernfoundry.common.TinkerDamageTypes;

@EventBusSubscriber(modid = TConstruct.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class WorldEvents{
    @SubscribeEvent
    static void mobKilled(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        LivingEntity dying = event.getEntity();
        boolean a = source.getDirectEntity() instanceof Snowball;
        boolean b = source.getDirectEntity() instanceof ThrownPotion potion && PotionUtils.getPotion(potion.getItem())== Potions.WATER && PotionUtils.getMobEffects(potion.getItem()).isEmpty();
        if (dying instanceof Blaze && (source.is(ModTags.DamageTypes.drop_ashes) || a || b)) {
            dropItem(dying,ModCommonItems.ashes.get());
        }
        if (source.is(DamageTypes.SONIC_BOOM)&&dying.hasEffect(ModEffects.holder(ModEffects.sculk_power))){
            if (dying instanceof Creeper){
                dropItem(dying,ModCommonItems.soul_vine.get().asItem());
            }
            if (dying instanceof IronGolem){
                dropItem(dying,ModCommonItems.bound_chain.get().asItem());
            }
        }
    }
    @SubscribeEvent
    static void blockBroke(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        if (!level.isClientSide() && event.getState().is(ModTags.Blocks.zith) && player != null){
            player.hurt(TinkerDamageTypes.source(level.registryAccess(), TinkerDamageTypes.BLEEDING), 20);
        }
    }
    private static void dropItem(LivingEntity dying, Item item){
        ItemEntity itementity = new ItemEntity(dying.level(), dying.getX(), dying.getY(), dying.getZ(), new ItemStack(item));
        itementity.setDefaultPickUpDelay();
        dying.level().addFreshEntity(itementity);
    }
    @SubscribeEvent
    static void villagerTrades(VillagerTradesEvent event) {
        // add ancient tools to the villager trader table
        if (event.getType().name().equals("weaponsmith")) {
            event.getTrades().get(5).add(emeraldToItemsTrade(ModToolItems.cutlass, Mth.nextInt(RandomSource.create(),25,45), 1, 5));
        }
    }
    public static BasicItemListing emeraldToItemsTrade(ItemLike item, int count, int maxTrades, int xp) {
        return new BasicItemListing(count, new ItemStack(item), maxTrades, xp, 0.2F);
    }
}
