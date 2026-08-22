package modernmods.modernfoundry.thinking.common.things.effect;

import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

import java.util.Objects;

public class FreezingColdEffect extends NoMilkEffect {
    public static final ResourceLocation FREEZINGCOLDBONUS = ResourceLocation.fromNamespaceAndPath("modernfoundry", "freezing_cold");
    public FreezingColdEffect(net.minecraft.world.effect.MobEffectCategory typeIn, int color, boolean show) {
        super(typeIn, color, show);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int tick, int amplifier) {
        return tick % 10 == 0 || tick == 1;
    }
    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        AttributeInstance attribute = living.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && attribute.getValue() > 0) {
            float x = (float) (-0.002 * (amplifier + 1));
            if (attribute.getModifier(FREEZINGCOLDBONUS) != null) {
                x += (float) Objects.requireNonNull(attribute.getModifier(FREEZINGCOLDBONUS)).amount();
                attribute.removeModifier(FREEZINGCOLDBONUS);
            }
            attribute.addTransientModifier(new AttributeModifier(FREEZINGCOLDBONUS, x, AttributeModifier.Operation.ADD_VALUE));
        }
        if (Objects.requireNonNull(living.getEffect(ModEffects.holder(ModEffects.freezing_cold))).getDuration()==1) {
            int y = 3 * amplifier + 3;
            LivingEntity lastAttacker = living.getLastHurtMob();
            DamageSource source = TinkerDamageTypes.source(living.level().registryAccess(), DamageTypes.FREEZE, lastAttacker);
            living.level().playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.GLASS_BREAK, SoundSource.MASTER, 1.0f, 1.0f);
            ToolAttackUtil.attackEntitySecondary(source, living.isInWaterOrRain() ? 2 * y : y, living, living, true);
            if (attribute != null) {
                attribute.removeModifier(FREEZINGCOLDBONUS);
            }
        }
        return true;
    }
}
