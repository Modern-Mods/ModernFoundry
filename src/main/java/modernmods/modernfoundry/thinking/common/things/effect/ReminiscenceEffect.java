package modernmods.modernfoundry.thinking.common.things.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import modernmods.modernfoundry.common.TinkerEffect;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;


import static net.minecraft.nbt.Tag.TAG_COMPOUND;

public class ReminiscenceEffect extends TinkerEffect {
    private static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath("modernfoundry", "n");
    public ReminiscenceEffect(net.minecraft.world.effect.MobEffectCategory typeIn, boolean show) {
        super(typeIn, 0xb83dba, show);
        NeoForge.EVENT_BUS.addListener((MobEffectEvent.Added event) -> this.onEffectAdded(event));
    }
    private void onEffectAdded(MobEffectEvent.Added event) {
        // store entity's current position when the effect is added
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && event.getOldEffectInstance() == null && event.getEffectInstance().getEffect().value() == this) {
            ModDataNBT data = PersistentDataCapability.getOrWarn(entity);
            CompoundTag health = new CompoundTag();
            health.putFloat("health",entity.getHealth());
            data.put(KEY, health);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        ModDataNBT data = PersistentDataCapability.getOrWarn(living);
        if (data.contains(KEY, TAG_COMPOUND)) {
            CompoundTag health = data.getCompound(KEY);
            if (living.isAlive()){
                living.setHealth(health.getFloat("health"));
            }
        }
        return true;
    }
}
