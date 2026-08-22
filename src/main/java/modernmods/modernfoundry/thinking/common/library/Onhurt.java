package modernmods.modernfoundry.thinking.common.library;

import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Objects;

public class Onhurt {
    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event){
        LivingEntity living = event.getEntity();
        Level level = living.level();
        DamageSource source = event.getSource();
        if (living.hasEffect(ModEffects.holder(ModEffects.cataclysm))){
            if (source.is(DamageTypeTags.IS_FIRE)&&!level.isClientSide){
                level.explode(living, living.getX(), living.getY(), living.getZ(), Objects.requireNonNull(living.getEffect(ModEffects.holder(ModEffects.cataclysm))).getAmplifier() + 1, Level.ExplosionInteraction.MOB);
                living.removeEffect(ModEffects.holder(ModEffects.cataclysm));
            }
            if (source.is(DamageTypes.FREEZE)){
                living.removeEffect(ModEffects.holder(ModEffects.cataclysm));
            }
        }
        if (living.hasEffect(ModEffects.holder(ModEffects.disarm))){
            living.removeEffect(ModEffects.holder(ModEffects.disarm));
        }
    }
}
