package modernmods.modernfoundry.thinking.common.library;

import modernmods.modernfoundry.thinking.common.register.ModCommonItems;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import modernmods.modernfoundry.thinking.data.ModDataKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;

import java.util.Objects;
import java.util.Optional;

public class OnDeath implements ModifierUtils {
    @SubscribeEvent
    public void onLivingDying(LivingDeathEvent event){
        LivingEntity living = event.getEntity();
        DamageSource source = event.getSource();
        if (living instanceof Player player && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            if (player.hasEffect(ModEffects.holder(ModEffects.last_effort))) {
                event.setCanceled(true);
                player.setHealth(1);
                block(living);
                if (player.getEffect(ModEffects.holder(ModEffects.last_effort)).getAmplifier() == 0) {
                    int x = Objects.requireNonNull(player.getEffect(ModEffects.holder(ModEffects.last_effort))).getDuration();
                    player.addEffect(new MobEffectInstance(ModEffects.holder(ModEffects.last_effort), x, 1));
                }
            } else {
                Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
                dataCap.ifPresent(data -> {
                    int level = data.get(ModDataKeys.SculkStruggle, 0);
                    if (level > 0 && player.hasEffect(ModEffects.holder(ModEffects.sculk_power))) {
                        event.setCanceled(true);
                        player.setHealth(1);
                        block(living);
                        player.addEffect(new MobEffectInstance(ModEffects.holder(ModEffects.last_effort), level * 60 + 180, 1));
                        if (player.level().isClientSide) {
                            ClientOnly.displayItemActivation();
                        }
                    }
                });
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ClientOnly {
        private static void displayItemActivation() {
            Minecraft.getInstance().gameRenderer.displayItemActivation(ModCommonItems.warden_steel.getIngot().getDefaultInstance());
        }
    }
}
