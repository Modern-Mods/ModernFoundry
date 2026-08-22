package modernmods.modernfoundry.integrations.proxy;

import net.neoforged.neoforge.api.distmarker.Dist;
import net.neoforged.neoforge.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import modernmods.modernfoundry.integrations.client.compat.Create;
import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.event.GameOverlayEventHandler;

@OnlyIn(Dist.CLIENT)
public final class ClientProxy extends CommonProxy {

    public ClientProxy() {}

    @Override
    public void registerListeners(IEventBus bus) {
        super.registerListeners(bus);

        bus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(new GameOverlayEventHandler());
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        if (ModList.get().isLoaded(ModIntegration.CREATE_MODID)) {
            Create.init();
        }
    }

}
