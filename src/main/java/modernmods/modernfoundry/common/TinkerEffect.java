package modernmods.modernfoundry.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import modernmods.modernfoundry.TConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Effect extension with a few helpers */
public class TinkerEffect extends MobEffect {
  /** All effects created, used to register client extensions (visibility) which are no longer set via the removed MobEffect#initializeClient */
  private static final List<TinkerEffect> ALL_EFFECTS = new ArrayList<>();

  /** If true, effect is visible, false for hidden */
  private final boolean show;
  public TinkerEffect(MobEffectCategory typeIn, boolean show) {
    this(typeIn, 0xffffff, show);
  }

  public TinkerEffect(MobEffectCategory typeIn, int color, boolean show) {
    super(typeIn, color);
    this.show = show;
    ALL_EFFECTS.add(this);
  }

  /** Registers client mob effect extensions for all Tinkers effects, replacing the removed MobEffect#initializeClient */
  @EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
  static class ClientExtensions {
    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
      for (TinkerEffect effect : ALL_EFFECTS) {
        boolean show = effect.show;
        event.registerMobEffect(new IClientMobEffectExtensions() {
          @Override
          public boolean isVisibleInInventory(MobEffectInstance instance) {
            return show;
          }

          @Override
          public boolean isVisibleInGui(MobEffectInstance instance) {
            return show;
          }
        }, effect);
      }
    }
  }

  // keep old call sites compact while targeting the holder-based 1.21 API
  public TinkerEffect addAttributeModifier(Attribute pAttribute, String pUuid, double pAmount, Operation pOperation) {
    super.addAttributeModifier(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(pAttribute), Identifier.fromNamespaceAndPath("modernfoundry", pUuid), pAmount, pOperation);
    return this;
  }

  public TinkerEffect addAttributeModifier(Holder<Attribute> attribute, String uuid, double amount, Operation operation) {
    super.addAttributeModifier(attribute, Identifier.fromNamespaceAndPath("modernfoundry", uuid), amount, operation);
    return this;
  }

  @Override
  public TinkerEffect addAttributeModifier(Holder<Attribute> attribute, Identifier id, double amount, Operation operation) {
    super.addAttributeModifier(attribute, id, amount, operation);
    return this;
  }

  /* Helpers */

  /**
   * Applies this potion to an entity
   * @param entity    Entity
   * @param duration  Duration
   * @return  Applied instance
   * @deprecated use {@link LivingEntity#addEffect(MobEffectInstance)}
   */
  @Deprecated
  public MobEffectInstance apply(LivingEntity entity, int duration) {
    return this.apply(entity, duration, 0);
  }

  /**
   * Applies this potion to an entity
   * @param entity    Entity
   * @param duration  Duration
   * @param level     Effect level
   * @return  Applied instance
   * @deprecated use {@link LivingEntity#addEffect(MobEffectInstance)}
   */
  @Deprecated
  public MobEffectInstance apply(LivingEntity entity, int duration, int level) {
    return this.apply(entity, duration, level, false);
  }

  /**
   * Applies this potion to an entity
   * @param entity    Entity
   * @param duration  Duration
   * @param amplifier Effect level
   * @param showIcon  If true, shows an icon in the HUD
   * @return  Applied instance
   * @deprecated use {@link LivingEntity#addEffect(MobEffectInstance)}
   */
  @Deprecated
  public MobEffectInstance apply(LivingEntity entity, int duration, int amplifier, boolean showIcon) {
    MobEffectInstance effect = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this), duration, amplifier, false, false, showIcon);
    entity.addEffect(effect);
    return effect;
  }

  /**
   * Gets the level of the effect on the entity starting from 1, or 0 if not active
   * @param entity  Entity to check
   * @return  Level, or 0 if inactive
   */
  public static int getLevel(LivingEntity entity, MobEffect effect) {
    return getAmplifier(entity, effect) + 1;
  }

  /** Gets the level of the effect on the entity starting from 1, or 0 if not active. */
  public static int getLevel(LivingEntity entity, Holder<MobEffect> effect) {
    return getAmplifier(entity, effect) + 1;
  }

  /**
   * Gets the level of the effect on the entity starting from 1, or 0 if not active
   * @param entity  Entity to check
   * @return  Level, or 0 if inactive
   */
  public static int getLevel(LivingEntity entity, Supplier<? extends MobEffect> effect) {
    return getAmplifier(entity, effect.get()) + 1;
  }

  /**
   * Gets the amplifier of the effect on the entity starting from 0, or -1 if not active
   * @param entity  Entity to check
   * @return  Amplifier, or -1 if inactive
   */
  public static int getAmplifier(LivingEntity entity, MobEffect effect) {
    return getAmplifier(entity, BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
  }

  /** Gets the amplifier of the effect on the entity starting from 0, or -1 if not active. */
  public static int getAmplifier(LivingEntity entity, Holder<MobEffect> effect) {
    MobEffectInstance instance = entity.getEffect(effect);
    if (instance != null) {
      return instance.getAmplifier();
    }
    return -1;
  }

  /** @deprecated use {@link #getAmplifier(LivingEntity, MobEffect)} which is better named or {@link #getLevel(LivingEntity, MobEffect)} which gives a more useful return */
  @Deprecated(forRemoval = true)
  public int getLevel(LivingEntity entity) {
    return getAmplifier(entity, this);
  }
}
