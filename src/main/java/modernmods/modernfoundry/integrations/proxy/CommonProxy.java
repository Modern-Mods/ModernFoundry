package modernmods.modernfoundry.integrations.proxy;

import net.minecraft.core.registries.Registries;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.RegisterEvent;

import modernmods.modernfoundry.integrations.common.ConsecrationTConstructModule;
import modernmods.modernfoundry.integrations.config.ConfigHandler;
import modernmods.modernfoundry.integrations.data.integration.CreateGogglesPredicate;
import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.items.TciIntegrationHooks;
import modernmods.modernfoundry.integrations.items.TciItems;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.common.network.TinkerNetwork;

public class CommonProxy {

    CommonProxy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigHandler.init();
        TciItems.init();
        TciModifiers.init();
        TciIntegrationHooks.init();
        registerListeners(bus);
        ModIntegration.setup();
    }

    public void registerListeners(IEventBus bus) {
        bus.register(Listeners.class);
    }

    public static final class Listeners {

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            TinkerNetwork.setup();
            TciItems.setup(event);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void registerItems(RegisterEvent event) {
            event.register(Registries.ITEM, ModIntegration::init);
        }

        @SubscribeEvent
        public static void imcEnqueue(final InterModEnqueueEvent event) {
            if (ModList.get().isLoaded(ModIntegration.CONSECRATION_MODID)) {
                ConsecrationTConstructModule.setup();
            }
            if (ModList.get().isLoaded(ModIntegration.CREATE_MODID)) {
                CreateGogglesPredicate.init();
            }
        }

    }

}
