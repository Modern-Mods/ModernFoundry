package modernmods.modernfoundry.integrations.proxy;

import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;


public final class ServerProxy extends CommonProxy {

    public ServerProxy() {
        TciIntegration.BUS.addListener(this::serverSetup);
    }

    private void serverSetup(FMLDedicatedServerSetupEvent event) {
    }

}
