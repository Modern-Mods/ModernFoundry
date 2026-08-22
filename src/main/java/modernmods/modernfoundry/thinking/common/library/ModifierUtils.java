package modernmods.modernfoundry.thinking.common.library;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.Sounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.library.modifiers.entity.ProjectileWithPower;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

import java.util.Objects;

public interface ModifierUtils {
    default void addEffect(LivingEntity living, MobEffect effect, int duration) {
        addEffect(living, BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, 0);
    }
    default void addEffect(LivingEntity living, MobEffect effect, int duration, int amplifier) {
        addEffect(living, BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, amplifier, true);
    }
    default void addEffect(LivingEntity living, MobEffect effect, int duration, int amplifier,boolean visible) {
        addEffect(living, BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, amplifier, visible);
    }
    default void addEffect(LivingEntity living, Holder<MobEffect> effect, int duration) {
        addEffect(living, effect, duration, 0);
    }
    default void addEffect(LivingEntity living, Holder<MobEffect> effect, int duration, int amplifier) {
        addEffect(living, effect, duration, amplifier, true);
    }
    default void addEffect(LivingEntity living, Holder<MobEffect> effect, int duration, int amplifier,boolean visible) {
        living.addEffect(new MobEffectInstance(effect, duration, amplifier, false, visible, visible));
    }
    ResourceLocation reverse_key = TConstruct.getResource("reverse");
    default void change(IToolStackView tool){
        ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
        if (reverse(tool)){
            persistentData.putBoolean(reverse_key, true);
        } else {
            persistentData.remove(reverse_key);
        }
    }
    default boolean reverse(IToolStackView tool){
        ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
        return !persistentData.contains(reverse_key);
    }
    default boolean reverseProjectile(Projectile projectile){
        ModDataNBT data = PersistentDataCapability.getOrWarn(projectile);
        return !data.getBoolean(reverse_key);
    }
    default void setPower(Projectile projectile,float multiple){
        float x= 1+multiple;
        if (projectile instanceof AbstractArrow arrow){
            arrow.setBaseDamage(arrow.getBaseDamage()*x);
        }
        else if (projectile instanceof ProjectileWithPower withPower){
            withPower.setPower(withPower.getPower()*x);
        }
    }
    default void addPower(Projectile projectile,float addition){
        if (projectile instanceof AbstractArrow arrow){
            arrow.setBaseDamage(arrow.getBaseDamage()+addition);
        }
        else if (projectile instanceof ProjectileWithPower projectile1){
            projectile1.setPower(projectile1.getPower()+addition);
        }
    }
    default void particles(Level level, LivingEntity living, ParticleOptions particleType){
        if (level instanceof ServerLevel server) {
            server.sendParticles(particleType, living.getRandomX(0.8), living.getRandomY() + 0.6, living.getRandomZ(0.8),0, 0.0F, 0.0F, 0.0F,0);
        }
    }
    default void particles(Level level, LivingEntity living, ParticleOptions particleType, int count){
        for (int i = 0 ; i<count ; i++){
            particles(level, living, particleType);
        }
    }
    default void heal(LivingEntity living, float amount){
        living.heal(amount);
        particles(living.level(), living, ParticleTypes.HAPPY_VILLAGER, 4);
        living.level().playSound(null, living.getX(), living.getY(), living.getZ(), Sounds.NECROTIC_HEAL.getSound(), living.getSoundSource(), 1.0f, 1.0f);
    }
    default void block(LivingEntity living){
        particles(living.level(), living, ParticleTypes.WAX_ON, 4);
        living.level().playSound(null, living.getX(), living.getY(), living.getZ(), Sounds.DAMAGE_BLOCKING.getSound(), living.getSoundSource(), 1.0f, 1.0f);
    }
}
