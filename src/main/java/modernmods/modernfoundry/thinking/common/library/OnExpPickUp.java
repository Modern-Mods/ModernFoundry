package modernmods.modernfoundry.thinking.common.library;

import modernmods.modernfoundry.thinking.common.register.ModEffects;
import modernmods.modernfoundry.thinking.data.ModDataKeys;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.bus.api.SubscribeEvent;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;

import java.util.Optional;

public class OnExpPickUp implements ModifierUtils {
    @SubscribeEvent
    public void onPlayerPickupXp(PlayerXpEvent.PickupXp event) {
        ExperienceOrb exp = event.getOrb();
        Player player = event.getEntity();
        Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(player).resolve();
        dataCap.ifPresent(data -> {
            if (data.get(ModDataKeys.SculkCatalyse, 0) > 0) {
                int time = exp.getValue() * 60;
                if (player.hasEffect(ModEffects.holder(ModEffects.sculk_power))) {
                    time = time + player.getEffect(ModEffects.holder(ModEffects.sculk_power)).getDuration() ;
                }
                addEffect(player, ModEffects.sculk_power.get(), time);
                event.setCanceled(true);
                exp.discard();
            }
        });
    }
}
