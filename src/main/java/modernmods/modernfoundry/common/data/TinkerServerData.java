package modernmods.modernfoundry.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import modernmods.modernfoundry.common.data.AdvancementsProvider;
import modernmods.modernfoundry.common.data.tags.BiomeTagProvider;
import modernmods.modernfoundry.common.data.tags.BlockEntityTypeTagProvider;
import modernmods.modernfoundry.common.data.tags.BlockTagProvider;
import modernmods.modernfoundry.common.data.tags.DamageTypeTagProvider;
import modernmods.modernfoundry.common.data.tags.EnchantmentTagProvider;
import modernmods.modernfoundry.common.data.tags.EntityTypeTagProvider;
import modernmods.modernfoundry.common.data.tags.FluidTagProvider;
import modernmods.modernfoundry.common.data.tags.ItemTagProvider;
import modernmods.modernfoundry.common.data.tags.MaterialTagProvider;
import modernmods.modernfoundry.common.data.tags.MenuTypeTagProvider;
import modernmods.modernfoundry.common.data.tags.ModifierTagProvider;
import modernmods.modernfoundry.common.data.tags.PotionTagProvider;
import modernmods.modernfoundry.gadgets.data.GadgetRecipeProvider;
import modernmods.modernfoundry.shared.data.CommonRecipeProvider;
import modernmods.modernfoundry.smeltery.data.SmelteryRecipeProvider;
import modernmods.modernfoundry.tables.data.TableRecipeProvider;
import modernmods.modernfoundry.tools.data.ModifierRecipeProvider;
import modernmods.modernfoundry.tools.data.ToolDefinitionDataProvider;
import modernmods.modernfoundry.tools.data.StationSlotLayoutProvider;
import modernmods.modernfoundry.tools.data.ToolsRecipeProvider;
import modernmods.modernfoundry.tools.data.material.MaterialRecipeProvider;
import modernmods.modernfoundry.world.data.WorldRecipeProvider;
import modernmods.modernfoundry.tools.data.material.MaterialDataProvider;
import modernmods.modernfoundry.tools.data.material.MaterialStatsDataProvider;
import modernmods.modernfoundry.tools.data.material.MaterialTraitsDataProvider;
import modernmods.modernfoundry.world.data.MobEquipmentProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Central registration of the SERVER-side data providers.
 * <p>
 * 26.1 split {@code GatherDataEvent} into {@code GatherDataEvent.Server} and {@code GatherDataEvent.Client}; the server
 * event runs via the {@code runServerData} run. Only providers that regenerate world-load data (tags, and later loot,
 * recipes, advancements, material/tool JSON) are registered here. The CLIENT model/sprite providers are wired
 * separately once ported to the vanilla model provider API.
 */
public final class TinkerServerData {
  private TinkerServerData() {}

  /** Registers all currently ported server data providers. Registered on the mod bus for {@link GatherDataEvent.Server}. */
  public static void gatherData(final GatherDataEvent.Server event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

    // tags: the block tag provider feeds its contents to the item tag provider for block->item tag copying
    BlockTagProvider blockTags = new BlockTagProvider(packOutput, lookupProvider);
    generator.addProvider(true, blockTags);
    generator.addProvider(true, new ItemTagProvider(packOutput, lookupProvider, blockTags.contentsGetter()));
    generator.addProvider(true, new FluidTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new EntityTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new BlockEntityTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new BiomeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new EnchantmentTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new MenuTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new PotionTagProvider(packOutput, lookupProvider));
    // TEMP-REGEN: DamageTypeTagProvider references TC damage types registered by the still-deferred DamageTypeProvider
    // registry-set; skip it for the partial recipe/tag regen so vanilla tag validation does not fail. Re-enable once
    // the registry-set providers are wired.
    // generator.addProvider(true, new DamageTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new MaterialTagProvider(packOutput));
    generator.addProvider(true, new ModifierTagProvider(packOutput));

    // material JSON: stats/traits providers consume the material list from the data provider
    MaterialDataProvider materials = new MaterialDataProvider(packOutput);
    generator.addProvider(true, materials);
    generator.addProvider(true, new MaterialStatsDataProvider(packOutput, materials));
    generator.addProvider(true, new MaterialTraitsDataProvider(packOutput, materials));

    // tool definition data (per-tool modules: parts, stats, traits, harvest, slots). Drives ToolDefinition.isDataLoaded()
    // — without it every tool shows "Missing tool data" and cannot render its parts.
    generator.addProvider(true, new ToolDefinitionDataProvider(packOutput));

    // station layouts (drive the tinker station / anvil / crafting-station GUIs — without them the menus fail to open)
    generator.addProvider(true, new StationSlotLayoutProvider(packOutput));
    // mob spawn equipment
    // TEMP-REGEN: builds fully-materialed render tool ItemStacks (needs bound item components, unavailable at
    // server datagen) — skipped for the partial recipe/tag/advancement regen.
    // generator.addProvider(true, new MobEquipmentProvider(packOutput));

    // advancements
    // TEMP-REGEN: AdvancementsProvider builds display-icon ItemStacks eagerly (needs bound item components,
    // unavailable at server datagen) — skipped for the partial recipe/tag regen.
    // generator.addProvider(true, new AdvancementsProvider(packOutput));

    // recipes (each former module's recipe provider)
    generator.addProvider(true, new CommonRecipeProvider(packOutput));
    generator.addProvider(true, new TableRecipeProvider(packOutput));
    generator.addProvider(true, new GadgetRecipeProvider(packOutput));
    generator.addProvider(true, new WorldRecipeProvider(packOutput));
    generator.addProvider(true, new ToolsRecipeProvider(packOutput));
    generator.addProvider(true, new MaterialRecipeProvider(packOutput));
    generator.addProvider(true, new ModifierRecipeProvider(packOutput));
    generator.addProvider(true, new SmelteryRecipeProvider(packOutput));
  }
}
