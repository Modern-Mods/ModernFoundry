package modernmods.modernfoundry.world;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.config.Config;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = TConstruct.MOD_ID)
public class WorldEvents {
  /* Heads */

  @SubscribeEvent
  static void livingVisibility(LivingVisibilityEvent event) {
    Entity lookingEntity = event.getLookingEntity();
    if (lookingEntity == null) {
      return;
    }
    ItemStack helmet = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
    Item item = helmet.getItem();
    if (item != Items.AIR && TinkerWorld.headItems.contains(item)) {
      if (lookingEntity.getType() == ((TinkerHeadType)((SkullBlock)((BlockItem)item).getBlock()).getType()).getType()) {
        event.modifyVisibility(0.5f);
      }
    }
  }

  @SubscribeEvent
  static void creeperKill(LivingDropsEvent event) {
    DamageSource source = event.getSource();
    if (source != null) {
      Entity entity = source.getEntity();
      // 26.1: canDropMobsSkull()/increaseDroppedSkulls() were removed; replicate with isPowered() + the droppedSkulls flag
      if (entity instanceof Creeper creeper && creeper.isPowered() && !creeper.droppedSkulls) {
        LivingEntity dying = event.getEntity();
        TinkerHeadType headType = TinkerHeadType.fromEntityType(dying.getType());
        if (headType != null && Config.COMMON.headDrops.get(headType).get() && dying.level() instanceof ServerLevel serverLevel) {
          creeper.droppedSkulls = true;
          ItemEntity drop = dying.spawnAtLocation(serverLevel, TinkerWorld.heads.get(headType));
          if (drop != null) {
            event.getDrops().add(drop);
          }
        }
      }
    }
  }

  // Note: the ancient tool wandering-trader injection was dropped here because NeoForge's WandererTradesEvent was removed
  // in 26.1 with no event replacement; wandering trades are now data-driven via TradeSets. Reintroducing the ancient tool
  // trade requires a data-driven trade set plus a custom loot function to build the random-material tool (see
  // AncientToolItemListing, which retains the offer-building logic).
}
