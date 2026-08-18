package modernmods.modernfoundry.smeltery;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterLoaders;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.mantle.client.render.ChannelFluids;
import modernmods.mantle.client.render.FaucetFluid;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.ClientEventBase;
import modernmods.modernfoundry.library.TinkerItemDisplays;
import modernmods.modernfoundry.library.client.model.block.FluidTextureModel;
import modernmods.modernfoundry.library.client.model.block.TankModel;
import modernmods.modernfoundry.library.client.model.tools.ToolModel;
import modernmods.modernfoundry.smeltery.client.render.CastingBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.render.ChannelBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.render.FaucetBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.render.GaugeBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.render.HeatingStructureBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.render.ProxyTankBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.render.TankBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.render.TankInventoryBlockEntityRenderer;
import modernmods.modernfoundry.smeltery.client.screen.AlloyerScreen;
import modernmods.modernfoundry.smeltery.client.screen.HeatingStructureScreen;
import modernmods.modernfoundry.smeltery.client.screen.MelterScreen;
import modernmods.modernfoundry.smeltery.client.screen.SingleItemScreenFactory;

@SuppressWarnings("unused")
@EventBusSubscriber(modid= TConstruct.MOD_ID, value= Dist.CLIENT)
public class SmelteryClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void addResourceListener(AddClientReloadListenersEvent event) {
    FaucetFluid.initialize(event);
    ChannelFluids.initialize(event);
  }

  @SubscribeEvent
  static void registerItemModels(net.neoforged.neoforge.client.event.RegisterItemModelsEvent event) {
    event.register(modernmods.modernfoundry.smeltery.client.model.TankItemModel.ID, modernmods.modernfoundry.smeltery.client.model.TankItemModel.Unbaked.MAP_CODEC);
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(TinkerSmeltery.tank.get(), TankBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.fluidCannon.get(), context -> new TankInventoryBlockEntityRenderer<>(BlockStateProperties.FACING));
    event.registerBlockEntityRenderer(TinkerSmeltery.faucet.get(), FaucetBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.channel.get(), ChannelBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.gauge.get(), GaugeBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.table.get(), CastingBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.basin.get(), CastingBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.proxyTank.get(), ProxyTankBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.melter.get(), context -> new TankInventoryBlockEntityRenderer<>(BlockStateProperties.HORIZONTAL_FACING));
    event.registerBlockEntityRenderer(TinkerSmeltery.alloyer.get(), TankBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.smeltery.get(), HeatingStructureBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.foundry.get(), HeatingStructureBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(TinkerSmeltery.castingTank.get(), context -> new TankInventoryBlockEntityRenderer<>(BlockStateProperties.HORIZONTAL_FACING));
  }

  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    ToolModel.registerSmallTool(TinkerItemDisplays.MELTER);
    ToolModel.registerSmallTool(TinkerItemDisplays.CASTING_BASIN);
    ToolModel.registerSmallTool(TinkerItemDisplays.CASTING_TABLE);
  }

  @SubscribeEvent
  static void registerMenuScreens(RegisterMenuScreensEvent event) {
    event.register(TinkerSmeltery.melterContainer.get(), MelterScreen::new);
    event.register(TinkerSmeltery.smelteryContainer.get(), HeatingStructureScreen::new);
    event.register(TinkerSmeltery.singleItemContainer.get(), new SingleItemScreenFactory());
    event.register(TinkerSmeltery.alloyerContainer.get(), AlloyerScreen::new);
  }

  @SubscribeEvent
  static void registerModelLoaders(RegisterLoaders event) {
    event.register(TConstruct.getResource("tank"), TankModel.LOADER);
    event.register(TConstruct.getResource("fluid_texture"), FluidTextureModel.LOADER);
  }
}
