package modernmods.modernfoundry.tools.modifiers.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.common.TinkerEffect;

/** TODO 1.21: move to {@link modernmods.modernfoundry.shared.effect} */
public class RepulsiveEffect extends TinkerEffect {
  public RepulsiveEffect() {
    super(MobEffectCategory.BENEFICIAL, 0x727272, false);
  }

  @Override
  public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
    return (duration & 1) == 0;
  }

  @Override
  public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
    MagneticEffect.applyVelocity(entity, amplifier, LivingEntity.class, 2, -0.1f, 10);
    return true;
  }
}
