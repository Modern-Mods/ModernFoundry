package modernmods.modernfoundry.tools.yoyo;


import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;

public class Interaction {
   public static boolean poisonEntity(ItemStack yoyoStack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target) {
      return applyEffect(yoyoStack, player, yoyo, target, MobEffects.POISON);
   }

   public static boolean glowEntity(ItemStack yoyoStack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target) {
      return applyEffect(yoyoStack, player, yoyo, target, MobEffects.GLOWING);
   }

   public static boolean enderiumEntity(ItemStack yoyoStack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target) {
      if (!canSpecialAttack(yoyoStack, player, yoyo, target)) {
         return false;
      }

      BlockPos pos = yoyo.blockPosition().offset(-128 + yoyo.level().random.nextInt(257), yoyo.level().random.nextInt(8), -128 + yoyo.level().random.nextInt(257));
      if (!yoyo.level().getBlockState(pos).isSolid()) {
         if (target instanceof LivingEntity living) {
            living.randomTeleport(pos.getX(), pos.getY(), pos.getZ(), true);
         } else if (target.level().getGameTime() % 40L == 0L) {
            target.setPos(pos.getX(), pos.getY(), pos.getZ());
            target.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
         }
      }
      return true;
   }

   private static boolean applyEffect(ItemStack yoyoStack, Player player, YoyoEntity yoyo, Entity target, Holder<net.minecraft.world.effect.MobEffect> effect) {
      if (!canSpecialAttack(yoyoStack, player, yoyo, target) || !(target instanceof LivingEntity living)) {
         return false;
      }
      living.addEffect(new MobEffectInstance(effect, 300));
      return true;
   }

   private static boolean canSpecialAttack(ItemStack yoyoStack, Player player, YoyoEntity yoyo, Entity target) {
      if (!yoyo.canAttack() || !target.isAlive() || !YoyoItem.isAttackEnable(yoyoStack) || !CommonHooks.onPlayerAttackTarget(player, target) || !target.isAttackable()) {
         return false;
      }
      UUID entityUUID = target.getUUID();
      return (!player.getShoulderEntityLeft().contains("UUID") || !entityUUID.equals(player.getShoulderEntityLeft().getUUID("UUID")))
         && (!player.getShoulderEntityRight().contains("UUID") || !entityUUID.equals(player.getShoulderEntityRight().getUUID("UUID")));
   }

   public static boolean collectItem(ItemStack yoyoStack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target) {
      if (target instanceof ItemEntity && yoyo.isCollecting()) {
         yoyo.collectDrop((ItemEntity)target);
         return true;
      } else {
         return false;
      }
   }

   public static boolean attackEntity(ItemStack yoyoStack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target) {
      if (!yoyo.canAttack() || !target.isAlive()) {
         return false;
      }

      if (!YoyoItem.isAttackEnable(yoyoStack)) {
         return false;
      }

      if (!CommonHooks.onPlayerAttackTarget(player, target)) {
         return false;
      }

      UUID entityUUID = target.getUUID();
      if ((!player.getShoulderEntityLeft().contains("UUID") || !entityUUID.equals(player.getShoulderEntityLeft().getUUID("UUID")))
         && (!player.getShoulderEntityRight().contains("UUID") || !entityUUID.equals(player.getShoulderEntityRight().getUUID("UUID")))) {
         if (target.isAttackable()) {
            if (!target.skipAttackInteraction(player)) {
               yoyo.resetAttackCooldown();
               yoyo.decrementRemainingTime(10);
               yoyo.getYoyo().damageItem(yoyoStack, hand, 1, player);
               float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
               DamageSource damagesource = player.damageSources().thrown(yoyo, player);
               float attackModifier = player.getEnchantedDamage(target, damage, damagesource);
               float strength = player.getAttackStrengthScale(0.5F);
               damage *= 0.2F + strength * strength * 0.8F;
               attackModifier *= strength;
               if (damage > 0.0F || attackModifier > 0.0F) {
                  boolean critical = attackModifier > 0.9F
                     && player.fallDistance > 0.0F
                     && !player.onGround()
                     && !player.onClimbable()
                     && !player.isInWater()
                     && !player.hasEffect(MobEffects.BLINDNESS)
                     && !player.isPassenger()
                     && target instanceof LivingEntity
                     && !player.isSprinting();
                  CriticalHitEvent hitResult = CommonHooks.fireCriticalHit(player, target, critical, critical ? 1.5F : 1.0F);
                  critical = hitResult.isCriticalHit();
                  damage += yoyoStack.getItem().getAttackDamageBonus(target, damage, damagesource);
                  if (critical) {
                     damage *= hitResult.getDamageMultiplier();
                  }

                  damage += attackModifier;
                  float targetHealth = target instanceof LivingEntity livingTarget ? livingTarget.getHealth() : 0.0F;
                  Vec3 motion = target.getDeltaMovement();
                  boolean didDamage = target.hurt(damagesource, damage);
                  if (didDamage) {
                     float knockbackModifier = player.getKnockback(target, damagesource);
                     if (knockbackModifier > 0.0F) {
                        if (target instanceof LivingEntity lv) {
                           lv.knockback(
                              knockbackModifier * 0.5F,
                              Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)),
                              -Mth.cos(player.getYRot() * (float) (Math.PI / 180.0))
                           );
                        } else {
                           player.push(
                              -Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)) * knockbackModifier * 0.5F,
                              0.1,
                              Mth.cos(player.getYRot() * (float) (Math.PI / 180.0)) * knockbackModifier * 0.5F
                           );
                        }

                        player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                        player.setSprinting(false);
                     }

                     if (target instanceof ServerPlayer && target.hurtMarked) {
                        ((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
                        target.hurtMarked = false;
                        target.setDeltaMovement(motion);
                     }

                     if (critical) {
                        player.level()
                           .playSound(null, yoyo.getX(), yoyo.getY(), yoyo.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                        player.crit(target);
                     }

                     if (!critical) {
                        if (attackModifier > 0.9F) {
                           player.level()
                              .playSound(null, yoyo.getX(), yoyo.getY(), yoyo.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);
                        } else {
                           player.level()
                              .playSound(null, yoyo.getX(), yoyo.getY(), yoyo.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, player.getSoundSource(), 1.0F, 1.0F);
                        }
                     }

                     if (attackModifier > 0.0F) {
                        player.magicCrit(target);
                     }

                     player.setLastHurtMob(target);
                     Entity entity = target;
                     if (target instanceof PartEntity) {
                        entity = ((PartEntity)target).getParent();
                     }

                     boolean flag5 = false;
                     if (target.level() instanceof ServerLevel sv) {
                        if (entity instanceof LivingEntity lv) {
                           flag5 = yoyoStack.hurtEnemy(lv, player);
                        }

                        EnchantmentHelper.doPostAttackEffects(sv, target, damagesource);
                     }

                     if (!player.level().isClientSide && !yoyoStack.isEmpty() && entity instanceof LivingEntity lv) {
                        ItemStack yoyoCopy = yoyoStack.copy();
                        boolean hurt = yoyoStack.hurtEnemy(lv, player);
                        EnchantmentHelper.doPostAttackEffects((ServerLevel)player.level(), target, damagesource);
                        if (hurt) {
                           yoyoStack.postHurtEnemy(lv, player);
                        }

                        if (yoyoStack.isEmpty()) {
                           EventHooks.onPlayerDestroyItem(player, yoyoCopy, hand);
                           player.setItemInHand(hand, ItemStack.EMPTY);
                        }
                     }

                     if (target instanceof LivingEntity) {
                        float damageDealt = targetHealth - ((LivingEntity)target).getHealth();
                        player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                        if (player.level() instanceof ServerLevel && damageDealt > 2.0F) {
                           int k = (int)(damageDealt * 0.5);
                           ((ServerLevel)player.level())
                              .sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), k, 0.1, 0.0, 0.1, 0.2);
                        }

                        player.causeFoodExhaustion(0.3F);
                     }
                  } else {
                     player.level()
                        .playSound(null, yoyo.getX(), yoyo.getY(), yoyo.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
                  }
               }
            } else {
               player.resetAttackStrengthTicker();
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean craftWithBlock(ItemStack yoyoStack, Player player, BlockPos pos, BlockState state, Block block, YoyoEntity yoyo) {
      if (!yoyo.canAttack()) {
         return false;
      } else if (yoyo.isRetracting()) {
         return false;
      } else if (player.level().isClientSide) {
         return false;
      } else {
         return !YoyoItem.isEnchantmentEnable(yoyoStack, YoyoEnchantments.CRAFTING, player.registryAccess()) ? false : false;
      }
   }

   public static boolean breakBlocks(ItemStack yoyoStack, Player player, BlockPos pos, BlockState state, Block block, YoyoEntity yoyo) {
      if (!yoyo.canAttack() && !player.isCreative()) {
         return false;
      }

      if (yoyo.isRetracting()) {
         return false;
      }

      if (player.level().isClientSide) {
         return false;
      }

      if (yoyoStack.getEnchantmentLevel(player.level().registryAccess().holderOrThrow(YoyoEnchantments.BREAKING)) < 1) {
         return false;
      }

      if (!YoyoItem.isEnchantmentEnable(yoyoStack, YoyoEnchantments.BREAKING, player.registryAccess())) {
         return false;
      }

      float blockSpeed = block.getDestroyProgress(state, player, player.level(), pos);
      if (block.defaultDestroyTime() >= 0.0F
         && blockSpeed <= ((YoyoItem)yoyoStack.getItem()).getTier().getSpeed()
         && blockSpeed >= 0.0F
         && block.canHarvestBlock(state, player.level(), pos, player)
         && destroyBlock((ServerLevel)player.level(), pos, (ServerPlayer)player, ((ServerPlayer)player).gameMode, yoyo)) {
         player.level().levelEvent(null, 2001, pos, Block.getId(state));
         yoyo.decrementRemainingTime(10);
         if (!player.isCreative()) {
            yoyo.resetAttackCooldown();
         }
      }

      return true;
   }

   public static boolean destroyBlock(ServerLevel level, BlockPos pos, ServerPlayer player, ServerPlayerGameMode gameMode, YoyoEntity yoyo) {
      BlockState blockstate1 = level.getBlockState(pos);
      BreakEvent event = CommonHooks.fireBlockBreak(level, gameMode.getGameModeForPlayer(), player, pos, blockstate1);
      if (event.isCanceled()) {
         return false;
      }

      BlockEntity blockentity = level.getBlockEntity(pos);
      Block block = blockstate1.getBlock();
      if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
         level.sendBlockUpdated(pos, blockstate1, blockstate1, 3);
         return false;
      }

      if (player.blockActionRestricted(level, pos, gameMode.getGameModeForPlayer())) {
         return false;
      }

      if (gameMode.isCreative()) {
         removeBlock(level, pos, false, player);
         return true;
      }

      BlockState blockstate = block.playerWillDestroy(level, pos, blockstate1, player);
      ItemStack itemstack = player.getMainHandItem();
      ItemStack itemstack1 = itemstack.copy();
      boolean flag1 = blockstate.canHarvestBlock(level, pos, player);
      itemstack.mineBlock(level, blockstate, pos, player);
      boolean flag = removeBlock(level, pos, flag1, player);
      if (flag && flag1) {
         if (!yoyo.isCollecting()) {
            block.playerDestroy(level, player, pos, blockstate, blockentity, itemstack1);
         } else {
            player.awardStat(Stats.BLOCK_MINED.get(block));
            player.causeFoodExhaustion(0.005F);
            Block.getDrops(blockstate, level, pos, blockentity, player, itemstack1).forEach(yoyo::createItemDropOrCollect);
            blockstate.spawnAfterBreak(level, pos, itemstack1, false);
         }
      }

      if (itemstack.isEmpty() && !itemstack1.isEmpty()) {
         EventHooks.onPlayerDestroyItem(player, itemstack1, InteractionHand.MAIN_HAND);
      }

      return true;
   }

   private static boolean removeBlock(Level level, BlockPos pos, boolean canHarvest, ServerPlayer player) {
      BlockState state = level.getBlockState(pos);
      boolean removed = state.onDestroyedByPlayer(level, pos, player, canHarvest, level.getFluidState(pos));
      if (removed) {
         state.getBlock().destroy(level, pos, state);
      }

      return removed;
   }
}
