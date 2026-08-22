package modernmods.modernfoundry.thinking.common.client;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.register.ModBlockEntities;
import modernmods.modernfoundry.thinking.common.register.ModToolItems;
import modernmods.modernfoundry.thinking.common.things.block.renderer.DryingRackBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.modernfoundry.library.client.model.TinkerItemProperties;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    public static void onConstruct() {
        ModBooks.initBook();
    }
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlockEntities.Drying_Rack.get(), DryingRackBlockEntityRenderer::new);
    }
    @SubscribeEvent
    static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TinkerItemProperties.registerToolProperties(ModToolItems.paxel.asItem());
            TinkerItemProperties.registerToolProperties(ModToolItems.knife.asItem());
            TinkerItemProperties.registerToolProperties(ModToolItems.mace.asItem());
            TinkerItemProperties.registerToolProperties(ModToolItems.cutlass.asItem());
            TinkerItemProperties.registerToolProperties(ModToolItems.atlatl.asItem());
            TinkerItemProperties.registerToolProperties(ModToolItems.magma_staff.asItem());
            TinkerItemProperties.registerToolProperties(ModToolItems.quartz_staff.asItem());
            TinkerItemProperties.registerToolProperties(ModToolItems.clay_staff.asItem());
            TinkerItemProperties.registerCrossbowProperties(ModToolItems.repeating_crossbow.asItem());
            EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        });
        ModBooks.FANTASTIC_GADGETRY.fontRenderer = unicodeFontRender();
    }
    private static Font unicodeRenderer;
    public static Font unicodeFontRender() {
        if (unicodeRenderer == null) {
            unicodeRenderer = Minecraft.getInstance().fontFilterFishy;
        }

        return unicodeRenderer;
    }
}
