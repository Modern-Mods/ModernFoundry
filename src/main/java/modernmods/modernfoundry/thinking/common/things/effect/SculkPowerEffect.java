package modernmods.modernfoundry.thinking.common.things.effect;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.modifer.defense.SculkBreedModifier;
import modernmods.modernfoundry.thinking.common.modifer.harvest.SculkBoostModule;
import modernmods.modernfoundry.thinking.common.modifer.harvest.SculkBoostModule;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import modernmods.modernfoundry.thinking.data.ModDataKeys;
import com.mojang.blaze3d.shaders.Effect;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

import java.util.Optional;

import static modernmods.modernfoundry.TConstruct.RANDOM;

public class SculkPowerEffect extends NoMilkEffect implements ModifierUtils {
    public SculkPowerEffect(MobEffectCategory typeIn, int color, boolean show) {
        super(typeIn, color, show);
        NeoForge.EVENT_BUS.addListener((MobEffectEvent.Added event) -> this.onEffectAdded(event));
        NeoForge.EVENT_BUS.addListener((MobEffectEvent.Remove event) -> this.onEffectRemove(event));
    }
    private void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        MobEffectInstance effect = event.getEffectInstance();
        if (effect.getEffect().value() == this){
            if (event.getOldEffectInstance() == null){
                Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(entity).resolve();
                dataCap.ifPresent(data -> {
                    int x = data.get(ModDataKeys.SculkBoost, 0);
                    if (x>0) entity.getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(SculkBoostModule.ATTRIBUTE_BONUS, x * 0.2f,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                });
            }
            if (!level.isClientSide() && (event.getOldEffectInstance() == null || effect.getDuration() > event.getOldEffectInstance().getDuration() + 2)){
                level.playSound(null, event.getEntity().getOnPos().above(), SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.PLAYERS, 1.0F, 1.6F + RANDOM.nextFloat() * 0.4F);
                particles(level, entity, new SculkChargeParticleOptions(0), 4);
            }
        }
    }
    private void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffect().value() == this) removeAttribute(event.getEntity());
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1;
    }
    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        removeAttribute(living);
        return true;
    }
    private void removeAttribute(LivingEntity living){
        AttributeInstance attribute1 = living.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attribute2 = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (attribute1 != null && attribute1.getModifier(SculkBreedModifier.ATTRIBUTE_BONUS) != null) {
            attribute1.removeModifier(SculkBreedModifier.ATTRIBUTE_BONUS);
            if (living.getHealth()>living.getMaxHealth()){
                living.setHealth(living.getMaxHealth());
            }
        }
        if (attribute2 != null && attribute2.getModifier(SculkBoostModule.ATTRIBUTE_BONUS) != null) {
            attribute2.removeModifier(SculkBoostModule.ATTRIBUTE_BONUS);
        }
    }
}
