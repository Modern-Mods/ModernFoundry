package modernmods.modernfoundry.plugin.jsonthings;

import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.model.TinkerItemProperties;
import modernmods.modernfoundry.library.client.model.tools.ToolModel;

/** Handles anything that requires clientside class loading */
public class PluginClient {
  public static void init() {
    // The removed runtime ItemColor system no longer applies; tool tints are baked into the model quads (see ToolModel).
    modernmods.modernfoundry.TConstruct.getModBus().addListener(PluginClient::clientSetup);
  }

  private static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
      for (Item item : FlexItemTypes.TOOL_ITEMS) {
        TinkerItemProperties.registerToolProperties(item);
      }
      for (Item item : FlexItemTypes.CROSSBOW_ITEMS) {
        TinkerItemProperties.registerCrossbowProperties(item);
      }
      for (Item item : FlexItemTypes.ARMOR_ITEMS) {
        TinkerItemProperties.registerBrokenProperty(item);
      }
    });
  }
}
