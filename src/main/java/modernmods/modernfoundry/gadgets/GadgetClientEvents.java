package modernmods.modernfoundry.gadgets;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterStandalone;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.ClientEventBase;
import modernmods.modernfoundry.gadgets.client.FancyItemFrameRenderer;
import modernmods.modernfoundry.gadgets.entity.shuriken.ShurikenEntityBase;
import modernmods.modernfoundry.tools.client.material.ThrownShurikenRenderer;

@SuppressWarnings("unused")
@EventBusSubscriber(modid=TConstruct.MOD_ID, value=Dist.CLIENT)
public class GadgetClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void registerModels(RegisterStandalone event) {
    // 26.1.2: the additional/standalone model registration API changed from ModelEvent.RegisterAdditional.register(ModelResourceLocation)
    // to RegisterStandalone.register(StandaloneModelKey, UnbakedStandaloneModel), and ModelResourceLocation was removed entirely.
    // The custom item-frame models are registered here once FancyItemFrameRenderer's frame-model rendering is restored in a later render pass.
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(TinkerGadgets.itemFrameEntity.get(), FancyItemFrameRenderer::new);
    EntityRendererProvider<ThrowableItemProjectile> throwable = ThrownItemRenderer::new;
    event.registerEntityRenderer(TinkerGadgets.glowBallEntity.get(), throwable);
    event.registerEntityRenderer(TinkerGadgets.eflnEntity.get(), throwable);
    EntityRendererProvider<ShurikenEntityBase> shuriken = ThrownShurikenRenderer::new;
    event.registerEntityRenderer(TinkerGadgets.quartzShurikenEntity.get(), shuriken);
    event.registerEntityRenderer(TinkerGadgets.flintShurikenEntity.get(), shuriken);
  }
}
