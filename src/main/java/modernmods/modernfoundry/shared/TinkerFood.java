package modernmods.modernfoundry.shared;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import modernmods.modernfoundry.shared.block.SlimeType;
import modernmods.modernfoundry.world.block.FoliageType;

/**
 * Food definitions. Since 26.1 status effects moved off {@link FoodProperties} and onto the
 * {@link Consumable} component, so each effect bearing food exposes a paired {@code *_CONSUMABLE}.
 */
@SuppressWarnings("WeakerAccess")
public final class TinkerFood {
  private TinkerFood() {}
  /** Bacon. What more is there to say? */
  public static final FoodProperties BACON = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.6F).build();

  /** Cheese is used for both the block and the ingot, eating the block returns 3 ingots */
  public static final FoodProperties CHEESE = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.4F).build();

  /** For the modifier */
  public static final FoodProperties JEWELED_APPLE = (new FoodProperties.Builder()).nutrition(4).saturationModifier(1.2F).alwaysEdible().build();
  public static final Consumable JEWELED_APPLE_CONSUMABLE = Consumables.defaultFood()
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HASTE, 1200, 0)))
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.RESISTANCE, 2400, 0)))
    .build();

  /* Cake block is set up to take food as a parameter */
  public static final FoodProperties EARTH_CAKE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).alwaysEdible().build();
  public static final Consumable EARTH_CAKE_CONSUMABLE = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.bouncy),      30 * 20, 0))).build();
  public static final FoodProperties SKY_CAKE   = new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).alwaysEdible().build();
  public static final Consumable SKY_CAKE_CONSUMABLE = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.doubleJump),  30 * 20, 0))).build();
  public static final FoodProperties ICHOR_CAKE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).alwaysEdible().build();
  public static final Consumable ICHOR_CAKE_CONSUMABLE = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.antigravity), 30 * 20, 0))).build();
  public static final FoodProperties ENDER_CAKE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.4f).alwaysEdible().build();
  public static final Consumable ENDER_CAKE_CONSUMABLE = Consumables.defaultFood().consumeSeconds(0.8f).onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.returning),   30 * 20, 0))).build();
  public static final FoodProperties MAGMA_CAKE = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).alwaysEdible().build();
  public static final Consumable MAGMA_CAKE_CONSUMABLE = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,      30 * 20, 0))).build();
  // regen is 50 ticks per half heart, so this heals 3 per slice
  public static final FoodProperties BLOOD_CAKE = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).alwaysEdible().build();
  public static final Consumable BLOOD_CAKE_CONSUMABLE = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 3 * 50, 0))).build();

  public static final FoodProperties EARTH_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  public static final Consumable EARTH_BOTTLE_CONSUMABLE = Consumables.defaultDrink()
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.experienced),  120 * 20, 0)))
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120 * 20, 1))).build();
  public static final FoodProperties SKY_BOTTLE   = new FoodProperties.Builder().alwaysEdible().build();
  public static final Consumable SKY_BOTTLE_CONSUMABLE = Consumables.defaultDrink()
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.ricochet),     120 * 20, 0)))
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120 * 20, 1))).build();
  public static final FoodProperties ICHOR_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  public static final Consumable ICHOR_BOTTLE_CONSUMABLE = Consumables.defaultDrink()
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.LEVITATION,             10 * 20, 0)))
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SLOWNESS,  10 * 20, 1))).build();
  public static final FoodProperties ENDER_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  public static final Consumable ENDER_BOTTLE_CONSUMABLE = Consumables.defaultDrink()
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.enderference),  60 * 20, 0)))
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SLOWNESS,  60 * 20, 1))).build();
  // 250 is 10 poison damage
  public static final FoodProperties VENOM_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  public static final Consumable VENOM_BOTTLE_CONSUMABLE = Consumables.defaultDrink()
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.STRENGTH, 30 * 20, 0)))
    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 250, 0))).build();
  /** @deprecated no longer used */
  @Deprecated(forRemoval = true)
  public static final FoodProperties MAGMA_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();

  public static final FoodProperties MEAT_SOUP = new FoodProperties.Builder().nutrition(8).saturationModifier(0.6f).build();

  /**
   * Gets the cake for the given slime type
   * @param slime  Slime type
   * @return  Cake food
   */
  public static FoodProperties getCake(FoliageType slime) {
    return switch (slime) {
      default -> EARTH_CAKE;
      case SKY -> SKY_CAKE;
      case ICHOR -> ICHOR_CAKE;
      case BLOOD -> BLOOD_CAKE;
      case ENDER -> ENDER_CAKE;
    };
  }

  /**
   * Gets the consumable effects for the cake of the given slime type
   * @param slime  Slime type
   * @return  Cake consumable
   */
  public static Consumable getCakeConsumable(FoliageType slime) {
    return switch (slime) {
      default -> EARTH_CAKE_CONSUMABLE;
      case SKY -> SKY_CAKE_CONSUMABLE;
      case ICHOR -> ICHOR_CAKE_CONSUMABLE;
      case BLOOD -> BLOOD_CAKE_CONSUMABLE;
      case ENDER -> ENDER_CAKE_CONSUMABLE;
    };
  }

  /**
   * Gets the bottle food for the given slime type
   * @param slime  Slime type
   * @return  Bottle food
   */
  public static FoodProperties getBottle(SlimeType slime) {
    return switch (slime) {
      default -> EARTH_BOTTLE;
      case SKY -> SKY_BOTTLE;
      case ICHOR -> ICHOR_BOTTLE;
      case ENDER -> ENDER_BOTTLE;
    };
  }

  /**
   * Gets the consumable effects for the bottle of the given slime type
   * @param slime  Slime type
   * @return  Bottle consumable
   */
  public static Consumable getBottleConsumable(SlimeType slime) {
    return switch (slime) {
      default -> EARTH_BOTTLE_CONSUMABLE;
      case SKY -> SKY_BOTTLE_CONSUMABLE;
      case ICHOR -> ICHOR_BOTTLE_CONSUMABLE;
      case ENDER -> ENDER_BOTTLE_CONSUMABLE;
    };
  }
}
