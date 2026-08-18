package modernmods.modernfoundry.fluids;

import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.neoforged.api.distmarker.Dist;
// 26.1: RegisterColorHandlersEvent.Item removed (item tints are data-driven now); potion color must be a tint source.
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.mantle.registration.object.FlowingFluidObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.ClientEventBase;
import modernmods.modernfoundry.library.client.model.FluidContainerModel;

@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class FluidClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    setTranslucent(TinkerFluids.honey);
    // slime
    setTranslucent(TinkerFluids.earthSlime);
    setTranslucent(TinkerFluids.skySlime);
    setTranslucent(TinkerFluids.enderSlime);
    // molten
    setTranslucent(TinkerFluids.moltenDiamond);
    setTranslucent(TinkerFluids.moltenEmerald);
    setTranslucent(TinkerFluids.moltenGlass);
    setTranslucent(TinkerFluids.moltenGlass);
    setTranslucent(TinkerFluids.liquidSoul);
    setTranslucent(TinkerFluids.moltenSoulsteel);
    setTranslucent(TinkerFluids.moltenAmethyst);
  }


  @SubscribeEvent
  static void registerItemModels(net.neoforged.neoforge.client.event.RegisterItemModelsEvent event) {
    event.register(FluidContainerModel.ID, FluidContainerModel.Unbaked.MAP_CODEC);
  }

  @SubscribeEvent
  static void registerFluidModels(net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
    // The potion fluid needs a per-stack (NBT) tint, which the data-driven fluid_texture color cannot express, so we
    // register its fluid model explicitly with PotionFluidTintSource. The still/flowing sprites come from the same
    // mantle/fluid_texture data used by every other Tinkers fluid.
    // PLAIN NOTE (validate in-game): this reads FluidTextureManager, which is a resource-reload listener; if it is not
    // yet populated when fluid models bake, swap these lookups for the literal sprite ids
    // modernfoundry:fluid/potion/still and modernfoundry:fluid/potion/flowing.
    net.neoforged.neoforge.fluids.FluidType type = TinkerFluids.potion.getType();
    net.minecraft.client.resources.model.sprite.Material still = new net.minecraft.client.resources.model.sprite.Material(modernmods.mantle.fluid.texture.FluidTextureManager.getStillTexture(type));
    net.minecraft.client.resources.model.sprite.Material flowing = new net.minecraft.client.resources.model.sprite.Material(modernmods.mantle.fluid.texture.FluidTextureManager.getFlowingTexture(type));
    event.register(new net.minecraft.client.renderer.block.FluidModel.Unbaked(still, flowing, null, modernmods.modernfoundry.fluids.fluids.PotionFluidTintSource.INSTANCE), TinkerFluids.potion.get());
  }

  private static void setTranslucent(FlowingFluidObject<?> fluid) {
    // 26.1: ItemBlockRenderTypes.setRenderLayer was removed; fluid/block render layers are data-driven now
    // (block render_type in the blockstate/model). Kept as a no-op so the setup call sites still compile;
    // translucency must be declared on the fluid block's model.
  }
}
