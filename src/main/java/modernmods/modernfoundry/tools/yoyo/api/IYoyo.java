package modernmods.modernfoundry.tools.yoyo.api;

import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IYoyo {
   double getWeight(ItemStack var1);

   double getLength(ItemStack var1);

   int getDuration(ItemStack var1);

   int getAttackInterval(ItemStack var1);

   int getMaxCollectedDrops(ItemStack var1, Provider var2);

   <T extends LivingEntity> void damageItem(ItemStack var1, InteractionHand var2, int var3, T var4);

   void entityInteraction(ItemStack var1, Player var2, InteractionHand var3, YoyoEntity var4, Entity var5);

   boolean interactsWithBlocks(ItemStack var1);

   void blockInteraction(ItemStack var1, Player var2, Level var3, BlockPos var4, BlockState var5, Block var6, YoyoEntity var7);

   default void onUpdate(ItemStack yoyoStack, YoyoEntity yoyo) {
   }

   default float getWaterMovementModifier(ItemStack yoyo) {
      return 0.3F;
   }

   @OnlyIn(Dist.CLIENT)
   default int getCordColor(ItemStack yoyo, float ticks) {
      return 14540253;
   }

   @OnlyIn(Dist.CLIENT)
   default RenderOrientation getRenderOrientation(ItemStack yoyo) {
      return RenderOrientation.Vertical;
   }
}


