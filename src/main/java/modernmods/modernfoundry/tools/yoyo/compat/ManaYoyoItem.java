package modernmods.modernfoundry.tools.yoyo.compat;

import java.lang.reflect.Method;
import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import modernmods.modernfoundry.tools.yoyo.YoyoTier;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Botania mana behavior kept reflection-safe because Botania is optional. */
public class ManaYoyoItem extends YoyoItem {
  private final Kind kind;

  public ManaYoyoItem(YoyoTier tier, Kind kind) {
    super(tier);
    this.kind = kind;
  }

  public enum Kind { MANA, ELEMENTIUM, MANA_TERRASTEEL, GAIA }

  @Override
  public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
    super.inventoryTick(stack, level, entity, slot, selected);
    if (!level.isClientSide && entity instanceof Player player && stack.getDamageValue() > 0
      && requestMana(stack, player, getManaPerDamage() * 2, true)) {
      stack.setDamageValue(stack.getDamageValue() - 1);
    }
  }

  @Override
  public <T extends LivingEntity> void damageItem(ItemStack stack, net.minecraft.world.InteractionHand hand, int amount, T entity) {
    if (entity instanceof Player player && requestMana(stack, player, getManaPerDamage() * amount, false)) {
      stack.hurtAndBreak(amount, entity, hand == net.minecraft.world.InteractionHand.OFF_HAND
        ? net.minecraft.world.entity.EquipmentSlot.OFFHAND : net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }
  }

  @Override
  public int getMaxCollectedDrops(ItemStack yoyo, Provider provider) {
    return switch (kind) {
      case ELEMENTIUM -> 64 + super.getMaxCollectedDrops(yoyo, provider);
      case GAIA -> 128 + super.getMaxCollectedDrops(yoyo, provider);
      default -> super.getMaxCollectedDrops(yoyo, provider);
    };
  }

  @Override
  public int getCordColor(ItemStack yoyo, float ticks) { return 10354676; }

  private int getManaPerDamage() { return kind == Kind.MANA_TERRASTEEL ? 100 : 60; }

  private static boolean requestMana(ItemStack stack, Player player, int amount, boolean simulate) {
    try {
      Class<?> handlerClass = Class.forName("vazkii.botania.api.mana.ManaItemHandler");
      Object handler = handlerClass.getMethod("instance").invoke(null);
      Method request = handler.getClass().getMethod("requestManaExactForTool", ItemStack.class, Player.class, int.class, boolean.class);
      return (Boolean)request.invoke(handler, stack, player, amount, simulate);
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      return false;
    }
  }
}
