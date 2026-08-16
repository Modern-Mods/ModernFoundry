package modernmods.modernfoundry.common.data.tags;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import modernmods.hilt.data.BuiltinRegistryTagProvider;
import modernmods.hilt.datagen.HiltTags;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.tables.TinkerTables;
import modernmods.modernfoundry.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class MenuTypeTagProvider extends BuiltinRegistryTagProvider<MenuType<?>> {
  @SuppressWarnings("deprecation")
  public MenuTypeTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, BuiltInRegistries.MENU, lookupProvider, TConstruct.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(Provider provider) {
    tag(HiltTags.MenuTypes.REPLACEABLE).add(
      // tool inventory allows really nice switching behavior
      TinkerTools.toolContainer.get(),
      TinkerTables.tinkerChestContainer.get(), TinkerSmeltery.singleItemContainer.get(),
      // unlike crafting table, our containers have persistent inventory; no progress loss\
      TinkerTables.craftingStationContainer.get(), TinkerTables.partBuilderContainer.get(), TinkerTables.tinkerStationContainer.get(), TinkerTables.modifierWorktableContainer.get(),
      TinkerSmeltery.melterContainer.get(), TinkerSmeltery.alloyerContainer.get(), TinkerSmeltery.smelteryContainer.get()
    );
    tag(TinkerTags.MenuTypes.TOOL_INVENTORY_REPLACEMENTS).addTag(HiltTags.MenuTypes.REPLACEABLE);
  }

  @Override
  public String getName() {
    return "Modern Foundry Menu Type Tags";
  }
}
