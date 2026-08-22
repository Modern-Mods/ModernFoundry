package modernmods.modernfoundry.integrations.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;

/**
 * Small boundary for compile-only integrations. Optional mods are never loaded
 * by a class reference in the base Modern Foundry path.
 */
public final class OptionalIntegrationHelper {
  private OptionalIntegrationHelper() {}

  public static Object staticField(String className, String fieldName) {
    try {
      Class<?> owner = Class.forName(className, false, OptionalIntegrationHelper.class.getClassLoader());
      return field(owner, null, fieldName);
    } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
      return null;
    }
  }

  public static Object field(Object target, String fieldName) {
    if (target == null) return null;
    try {
      return field(target.getClass(), target, fieldName);
    } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
      return null;
    }
  }

  public static boolean setField(Object target, String fieldName, Object value) {
    if (target == null) return false;
    try {
      Field field;
      try {
        field = target.getClass().getField(fieldName);
      } catch (NoSuchFieldException exception) {
        field = target.getClass().getDeclaredField(fieldName);
        field.trySetAccessible();
      }
      field.set(target, value);
      return true;
    } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
      return false;
    }
  }

  private static Object field(Class<?> owner, Object target, String fieldName) throws ReflectiveOperationException {
    Field field;
    try {
      field = owner.getField(fieldName);
    } catch (NoSuchFieldException exception) {
      field = owner.getDeclaredField(fieldName);
      field.trySetAccessible();
    }
    return field.get(target);
  }

  public static Object invoke(Object target, String methodName, Object... arguments) {
    if (target == null) return null;
    return invoke(target instanceof Class<?> type ? type : target.getClass(), target instanceof Class<?> ? null : target,
        methodName, arguments);
  }

  public static Object invokeStatic(String className, String methodName, Object... arguments) {
    try {
      Class<?> owner = Class.forName(className, false, OptionalIntegrationHelper.class.getClassLoader());
      return invoke(owner, null, methodName, arguments);
    } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
      return null;
    }
  }

  private static Object invoke(Class<?> owner, Object target, String methodName, Object... arguments) {
    try {
      Method selected = null;
      for (Method method : owner.getMethods()) {
        if (method.getName().equals(methodName) && compatible(method.getParameterTypes(), arguments)) {
          selected = method;
          break;
        }
      }
      if (selected == null) {
        for (Method method : owner.getDeclaredMethods()) {
          if (method.getName().equals(methodName) && compatible(method.getParameterTypes(), arguments)) {
            method.trySetAccessible();
            selected = method;
            break;
          }
        }
      }
      return selected == null ? null : selected.invoke(target, arguments);
    } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
      return null;
    }
  }

  public static Object newInstance(String className, Object... arguments) {
    try {
      Class<?> owner = Class.forName(className, false, OptionalIntegrationHelper.class.getClassLoader());
      for (Constructor<?> constructor : owner.getConstructors()) {
        if (compatible(constructor.getParameterTypes(), arguments)) {
          return constructor.newInstance(arguments);
        }
      }
      for (Constructor<?> constructor : owner.getDeclaredConstructors()) {
        if (compatible(constructor.getParameterTypes(), arguments)) {
          constructor.trySetAccessible();
          return constructor.newInstance(arguments);
        }
      }
    } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
      // Optional integrations are allowed to disappear between compile and runtime.
    }
    return null;
  }

  private static boolean compatible(Class<?>[] parameterTypes, Object[] arguments) {
    if (parameterTypes.length != arguments.length) return false;
    for (int index = 0; index < parameterTypes.length; index++) {
      Object argument = arguments[index];
      if (argument == null) {
        if (parameterTypes[index].isPrimitive()) return false;
      } else if (!box(parameterTypes[index]).isInstance(argument)) {
        return false;
      }
    }
    return true;
  }

  private static Class<?> box(Class<?> type) {
    if (!type.isPrimitive()) return type;
    if (type == boolean.class) return Boolean.class;
    if (type == byte.class) return Byte.class;
    if (type == short.class) return Short.class;
    if (type == int.class) return Integer.class;
    if (type == long.class) return Long.class;
    if (type == float.class) return Float.class;
    if (type == double.class) return Double.class;
    if (type == char.class) return Character.class;
    return type;
  }

  public static Object unwrap(Object value) {
    Object current = value;
    for (int depth = 0; current != null && depth < 4; depth++) {
      if (current instanceof Optional<?> optional) {
        current = optional.orElse(null);
        continue;
      }
      Object present = invoke(current, "isPresent");
      if (present instanceof Boolean booleanValue && booleanValue) {
        Object unwrapped = invoke(current, "orElse", new Object[] {null});
        if (unwrapped != null && unwrapped != current) {
          current = unwrapped;
          continue;
        }
      }
      Object supplied = invoke(current, "get");
      if (supplied != null && supplied != current && !(current instanceof Holder<?>)) {
        current = supplied;
        continue;
      }
      break;
    }
    return current;
  }

  public static double number(Object value, double fallback) {
    Object unwrapped = unwrap(value);
    return unwrapped instanceof Number number ? number.doubleValue() : fallback;
  }

  public static boolean bool(Object value) {
    return Boolean.TRUE.equals(unwrap(value));
  }

  public static Object capability(Object target, String className, String fieldName) {
    Object capability = unwrap(staticField(className, fieldName));
    return unwrap(invoke(target, "getCapability", capability));
  }

  public static double configNumber(String className, String section, String fieldName, double fallback) {
    return number(field(staticField(className, section), fieldName), fallback);
  }

  public static <T> T registry(Registry<T> registry, String id) {
    try {
      return registry.getOptional(ResourceLocation.parse(id)).orElse(null);
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  public static <T> Holder<T> holder(Registry<T> registry, String id) {
    try {
      return registry.getHolder(ResourceLocation.parse(id)).map(value -> (Holder<T>) value).orElse(null);
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  public static Holder<Enchantment> enchantmentHolder(String className, String fieldName, String fallbackId) {
    Object value = staticField(className, fieldName);
    Object unwrapped = unwrap(value);
    if (unwrapped instanceof Holder<?> holder) return (Holder<Enchantment>) holder;
    return null;
  }

  public static void setEnchantment(ItemStack stack, Holder<Enchantment> enchantment, int level) {
    if (stack == null || enchantment == null) return;
    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
    mutable.set(enchantment, level);
    EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
  }

  public static Holder<MobEffect> effectHolder(String className, String fieldName, String fallbackId) {
    Object value = staticField(className, fieldName);
    Object unwrapped = unwrap(value);
    if (unwrapped instanceof Holder<?> holder) return (Holder<MobEffect>) holder;
    return holder(BuiltInRegistries.MOB_EFFECT, fallbackId);
  }

  public static Holder<Attribute> attributeHolder(String className, String fieldName, String fallbackId) {
    Object value = staticField(className, fieldName);
    Object unwrapped = unwrap(value);
    if (unwrapped instanceof Holder<?> holder) return (Holder<Attribute>) holder;
    if (unwrapped instanceof Attribute attribute) {
      ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
      return id == null ? null : BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
    }
    return holder(BuiltInRegistries.ATTRIBUTE, fallbackId);
  }

  public static SoundEvent sound(String id) { return registry(BuiltInRegistries.SOUND_EVENT, id); }
  public static Block block(String id) { return registry(BuiltInRegistries.BLOCK, id); }
  public static Item item(String id) { return registry(BuiltInRegistries.ITEM, id); }

  public static boolean isEntity(Entity entity, String id) {
    return entity != null && ResourceLocation.parse(id).equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
  }

  public static boolean isEntity(Entity entity, String... ids) {
    if (entity == null) return false;
    ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    if (key == null) return false;
    for (String id : ids) {
      if (key.equals(ResourceLocation.parse(id))) return true;
    }
    return false;
  }

  public static Entity create(EntityType<?> type, net.minecraft.world.level.Level level) {
    return type == null ? null : type.create(level);
  }
}
