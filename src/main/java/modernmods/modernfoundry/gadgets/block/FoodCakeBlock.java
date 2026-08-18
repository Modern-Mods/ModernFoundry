package modernmods.modernfoundry.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Extension of cake that utilizes a food instance for properties
 */
public class FoodCakeBlock extends CakeBlock {
  private final FoodProperties food;
  private final Consumable consumable;
  private final EffectCombination combination;

  public FoodCakeBlock(Properties properties, FoodProperties food, Consumable consumable, EffectCombination combination) {
    super(properties);
    this.food = food;
    this.consumable = consumable;
    this.combination = combination;
  }

  /** Gets the consumable holding the effects applied when eating this cake; used by the block item for its tooltip */
  public Consumable getConsumable() {
    return consumable;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
    InteractionResult result = this.eatSlice(world, pos, state, player);
    if (result.consumesAction()) {
      return result;
    }
    if (world.isClientSide() && player.getMainHandItem().isEmpty()) {
      return InteractionResult.CONSUME;
    }
    return InteractionResult.PASS;
  }

  /** Checks if the given player has all potion effects from the food */
  private boolean hasAllEffects(Player player) {
    for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
      if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect apply) {
        for (MobEffectInstance effect : apply.effects()) {
          MobEffectInstance current = player.getEffect(effect.getEffect());
          if (current == null || current.getDuration() < 100) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /** Eats a single slice of cake if possible */
  private InteractionResult eatSlice(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
    if (!player.canEat(false) && !food.canAlwaysEat()) {
      return InteractionResult.PASS;
    }
    // repurpose fast eating, will mean no eating if we have the effect
    if (combination == EffectCombination.BLOCK && hasAllEffects(player)) {
      return InteractionResult.PASS;
    }
    player.awardStat(Stats.EAT_CAKE_SLICE);
    // apply food stats
    player.getFoodData().eat(food.nutrition(), food.saturation());
    for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
      if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect apply) {
        if (!world.isClientSide() && world.getRandom().nextFloat() < apply.probability()) {
          for (MobEffectInstance base : apply.effects()) {
            MobEffectInstance effect = base;
            // if adding, increase duration by current duration, provided its an exact level match
            if (combination == EffectCombination.ADD) {
              MobEffectInstance current = player.getEffect(effect.getEffect());
              if (current != null && current.getAmplifier() == effect.getAmplifier()) {
                effect = new MobEffectInstance(effect.getEffect(), effect.getDuration() + current.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
              }
            }
            player.addEffect(effect);
          }
        }
      }
    }
    // remove one bite from the cake
    int i = state.getValue(BITES);
    if (i < 6) {
      world.setBlock(pos, state.setValue(BITES, i + 1), 3);
    } else {
      world.removeBlock(pos, false);
    }
    return InteractionResult.SUCCESS;
  }

  public enum EffectCombination {
    /** New effect will update time on existing, like potions */
    SET,
    /** New effect will increase duration of existing */
    ADD,
    /** Cake cannot be eaten if effect is present  */
    BLOCK
  }
}
