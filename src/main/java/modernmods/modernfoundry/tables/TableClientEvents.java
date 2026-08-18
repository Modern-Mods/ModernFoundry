package modernmods.modernfoundry.tables;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import modernmods.modernfoundry.compat.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
// 26.1: block/item color handlers removed (data-driven BlockTintSources/ItemTintSources in model JSON);
// the tinkers-chest dye color must be re-expressed as a tint source. Old handlers dropped so the mod loads.
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.mantle.client.render.InventoryBlockEntityRenderer;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.ClientEventBase;
import modernmods.modernfoundry.shared.block.entity.TableBlockEntity;
import modernmods.modernfoundry.tables.block.entity.chest.TinkersChestBlockEntity;
import modernmods.modernfoundry.tables.client.inventory.CraftingStationScreen;
import modernmods.modernfoundry.tables.client.inventory.ModifierWorktableScreen;
import modernmods.modernfoundry.tables.client.inventory.PartBuilderScreen;
import modernmods.modernfoundry.tables.client.inventory.TinkerChestScreen;
import modernmods.modernfoundry.tables.client.inventory.TinkerStationScreen;

@SuppressWarnings("unused")
@EventBusSubscriber(modid=TConstruct.MOD_ID, value=Dist.CLIENT)
public class TableClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    BlockEntityRendererProvider<TableBlockEntity, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> tableRenderer = InventoryBlockEntityRenderer::new;
    event.registerBlockEntityRenderer(TinkerTables.craftingStationTile.get(), tableRenderer);
    event.registerBlockEntityRenderer(TinkerTables.tinkerStationTile.get(), tableRenderer);
    event.registerBlockEntityRenderer(TinkerTables.modifierWorktableTile.get(), tableRenderer);
    event.registerBlockEntityRenderer(TinkerTables.partBuilderTile.get(), tableRenderer);
  }

  @SubscribeEvent
  static void setupClient(final FMLClientSetupEvent event) {}

  @SubscribeEvent
  static void registerMenuScreens(RegisterMenuScreensEvent event) {
    event.register(TinkerTables.craftingStationContainer.get(), CraftingStationScreen::new);
    event.register(TinkerTables.tinkerStationContainer.get(), TinkerStationScreen::new);
    event.register(TinkerTables.partBuilderContainer.get(), PartBuilderScreen::new);
    event.register(TinkerTables.modifierWorktableContainer.get(), ModifierWorktableScreen::new);
    event.register(TinkerTables.tinkerChestContainer.get(), TinkerChestScreen::new);
  }


}
