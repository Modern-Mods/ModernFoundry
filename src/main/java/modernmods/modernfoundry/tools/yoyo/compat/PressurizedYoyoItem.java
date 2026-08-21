package modernmods.modernfoundry.tools.yoyo.compat;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import modernmods.modernfoundry.tools.yoyo.YoyoTier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** PneumaticCraft behavior kept optional and reflection-safe. */
public final class PressurizedYoyoItem extends YoyoItem {
  private static final int VOLUME = 3000;

  public PressurizedYoyoItem(YoyoTier tier) { super(airProperties(), tier); }

  private static Item.Properties airProperties() {
    Item.Properties properties = new Item.Properties().stacksTo(1);
    DataComponentType<?> air = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "air"));
    if (air != null) {
      @SuppressWarnings({"rawtypes", "unchecked"}) DataComponentType<Integer> typedAir = (DataComponentType)air;
      properties.component(typedAir, 0);
    }
    return properties;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide && getPressure(stack) > 0.1F) {
      YoyoEntity yoyo = YoyoEntity.CASTERS.get(player.getUUID());
      if (yoyo == null) {
        yoyo = this.factory.create(level, player, hand);
        level.addFreshEntity(yoyo);
        level.playSound(null, yoyo.getX(), yoyo.getY(), yoyo.getZ(), Sounds.YOYO_THROW.getSound(), SoundSource.NEUTRAL, 0.5F, 1.0F);
        player.causeFoodExhaustion(0.05F);
      } else {
        yoyo.setRetracting(!yoyo.isRetracting());
      }
    }
    return InteractionResultHolder.success(stack);
  }

  @Override
  public boolean isBarVisible(ItemStack stack) { return getPressure(stack) < getMaxPressure(stack); }

  @Override
  public int getBarWidth(ItemStack stack) { return Math.round(getPressure(stack) / getMaxPressure(stack) * 13.0F); }

  @Override
  public int getBarColor(ItemStack stack) {
    float pressure = getPressure(stack) / getMaxPressure(stack);
    int c = (int)(64.0F + 191.0F * pressure);
    return 4194304 | c << 8 | 0xFF;
  }

  @Override
  public <T extends LivingEntity> void damageItem(ItemStack stack, InteractionHand hand, int amount, T entity) {
    airHandler(stack).ifPresent(handler -> invoke(handler, "addAir", new Class<?>[]{int.class}, -amount * 50));
  }

  private static float getPressure(ItemStack stack) {
    return airHandler(stack).map(handler -> ((Number)invoke(handler, "getPressure")).floatValue()).orElse(0.0F);
  }

  private static float getMaxPressure(ItemStack stack) {
    return airHandler(stack).map(handler -> ((Number)invoke(handler, "maxPressure")).floatValue()).orElse(1.0F);
  }

  private static Optional<Object> airHandler(ItemStack stack) {
    try {
      Class<?> capabilities = Class.forName("me.desht.pneumaticcraft.api.PNCCapabilities");
      Object result = capabilities.getMethod("getAirHandler", ItemStack.class).invoke(null, stack);
      return result instanceof Optional<?> optional ? optional.map(value -> value) : Optional.empty();
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      return Optional.empty();
    }
  }

  private static Object invoke(Object target, String method, Class<?>... types) {
    try {
      return target.getClass().getMethod(method, types).invoke(target);
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      return 0.0F;
    }
  }

  private static Object invoke(Object target, String method, Class<?>[] types, Object... args) {
    try {
      return target.getClass().getMethod(method, types).invoke(target, args);
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      return null;
    }
  }
}
