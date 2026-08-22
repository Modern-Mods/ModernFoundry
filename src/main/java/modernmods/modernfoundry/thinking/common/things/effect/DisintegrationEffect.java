package modernmods.modernfoundry.thinking.common.things.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

public class DisintegrationEffect extends NoMilkEffect {
    public DisintegrationEffect(net.minecraft.world.effect.MobEffectCategory typeIn, int color, boolean show) {
        super(typeIn, color, show);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int tick, int amplifier) {
        return tick > 0 && tick % 20 == 0;
    }
    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        LivingEntity lastAttacker = living.getLastHurtMob();
        DamageSource source = TinkerDamageTypes.source(living.level().registryAccess(), TinkerDamageTypes.BLEEDING, lastAttacker);
        ToolAttackUtil.attackEntitySecondary(source, (float) (living.getMaxHealth()*0.01*(amplifier+1)), living, living, true);
        return true;
    }
}
