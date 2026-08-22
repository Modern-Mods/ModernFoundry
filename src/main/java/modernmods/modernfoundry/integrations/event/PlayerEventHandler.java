package modernmods.modernfoundry.integrations.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;
import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.network.ArsElementalSetData;
import modernmods.modernfoundry.integrations.network.BotaniaSetData;
import modernmods.modernfoundry.common.network.TinkerNetwork;

public class PlayerEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        final Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;

            if (ModList.get().isLoaded(ModIntegration.BOTANIA_MODID)) {
                CapabilityRegistry.getBotania(sp).ifPresent(data -> {
                    TinkerNetwork.getInstance().sendTo(new BotaniaSetData(data.hasTerrestrial(), data.hasGreatFairy(), data.hasAlfheim()), sp);
                });
            }
            if (ModList.get().isLoaded(ModIntegration.ARS_ELEMENTAL_MODID)) {
                CapabilityRegistry.getArsElemental(sp).ifPresent(data -> {
                    TinkerNetwork.getInstance().sendTo(new ArsElementalSetData(data.hasAir(), data.hasAqua(), data.hasEarth(), data.hasFire()), sp);
                });
            }
        }
    }

}
