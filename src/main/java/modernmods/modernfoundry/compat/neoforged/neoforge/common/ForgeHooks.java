package modernmods.modernfoundry.compat.neoforged.neoforge.common;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class ForgeHooks {
  private ForgeHooks() {}

  @Nullable
  public static float[] onLivingFall(LivingEntity entity, float distance, float damageMultiplier) {
    // 26.1.2: CommonHooks.onLivingFall now returns the (posted) event; rebuild the legacy float[] contract.
    LivingFallEvent event = CommonHooks.onLivingFall(entity, distance, damageMultiplier);
    if (event.isCanceled()) {
      return null;
    }
    return new float[]{(float) event.getDistance(), event.getDamageMultiplier()};
  }

  public static void setCraftingPlayer(Player player) {
    CommonHooks.setCraftingPlayer(player);
  }

  public static Player getCraftingPlayer() {
    return CommonHooks.getCraftingPlayer();
  }

  public static int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
    // 26.1.2: burn times are data-driven via FuelValues, sourced from the running server (fuel checks are server-side).
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) {
      return 0;
    }
    FuelValues fuelValues = server.fuelValues();
    return EventHooks.getItemBurnTime(stack, stack.getBurnTime(recipeType, fuelValues), recipeType, fuelValues);
  }

  public static ItemStack getProjectile(LivingEntity entity, ItemStack bow, ItemStack projectile) {
    return CommonHooks.getProjectile(entity, bow, projectile);
  }

  public static CriticalHitEvent getCriticalHit(Player player, Entity target, boolean vanillaCritical, float damageModifier) {
    return CommonHooks.fireCriticalHit(player, target, vanillaCritical, damageModifier);
  }

  @Nullable
  public static InteractionResult onInteractEntityAt(Player player, Entity entity, HitResult ray, InteractionHand hand) {
    return CommonHooks.onInteractEntityAt(player, entity, ray, hand);
  }

  public static PlayerInteractEvent.RightClickBlock onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
    return CommonHooks.onRightClickBlock(player, hand, pos, hitVec);
  }

  public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(Player player, BlockPos pos, net.minecraft.core.Direction face, ServerboundPlayerActionPacket.Action action) {
    return CommonHooks.onLeftClickBlock(player, pos, face, action);
  }

  public static int onBlockBreakEvent(ServerLevel level, GameType gameType, ServerPlayer player, BlockPos pos) {
    var state = level.getBlockState(pos);
    var event = CommonHooks.fireBlockBreak(level, gameType, player, pos, state);
    if (event.isCanceled()) {
      return -1;
    }
    return state.getExpDrop(level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
  }
}
