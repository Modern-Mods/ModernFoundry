package modernmods.modernfoundry.tools.modifiers.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.tools.modifiers.traits.melee.LaceratingModifier;

/**
 * Potion effect from {@link LaceratingModifier}
 * TODO 1.21: move to {@link modernmods.modernfoundry.shared.effect}
 */
public class BleedingEffect extends NoMilkEffect {
  public BleedingEffect() {
    super(MobEffectCategory.HARMFUL, 0xa80000, true);
  }

  @Override
  public boolean shouldApplyEffectTickThisTick(int tick, int level) {
    // every half second
    return tick > 0 && tick % 20 == 0;
  }

  @Override
  public boolean applyEffectTick(LivingEntity target, int level) {
    // attribute to player kill
    LivingEntity lastAttacker = target.getLastHurtMob();
    DamageSource source = TinkerDamageTypes.source(target.level().registryAccess(), TinkerDamageTypes.BLEEDING, lastAttacker);

    // perform damage
    int hurtResistantTime = target.invulnerableTime;
    ToolAttackUtil.attackEntitySecondary(source, 1, target, target, true);
    target.invulnerableTime = hurtResistantTime;

    // damage particles
    if (target.level() instanceof ServerLevel serverLevel) {
      serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), 1, 0.1, 0, 0.1, 0.2);
    }
    return true;
  }
}
