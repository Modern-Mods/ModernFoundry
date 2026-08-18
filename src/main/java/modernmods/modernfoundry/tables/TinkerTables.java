package modernmods.modernfoundry.tables;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.mantle.recipe.helper.SimpleRecipeSerializer;
import modernmods.mantle.registration.object.ItemObject;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerModule;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipe;
import modernmods.modernfoundry.library.recipe.material.ShapedMaterialRecipe;
import modernmods.modernfoundry.library.recipe.material.ShapedMaterialsRecipe;
import modernmods.modernfoundry.library.recipe.material.ShapelessMaterialsRecipe;
import modernmods.modernfoundry.library.recipe.partbuilder.ItemPartRecipe;
import modernmods.modernfoundry.library.recipe.partbuilder.PartRecipe;
import modernmods.modernfoundry.library.recipe.partbuilder.recycle.PartBuilderRecycle;
import modernmods.modernfoundry.library.recipe.tinkerstation.building.FixedMaterialSwappingRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.building.PartSwappingOverrideRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.building.ToolMaterialSwappingRecipe;
import modernmods.modernfoundry.library.tools.layout.StationSlotLayoutLoader;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.shared.block.TableBlock;
import modernmods.modernfoundry.tables.block.ChestBlock;
import modernmods.modernfoundry.tables.block.CraftingStationBlock;
import modernmods.modernfoundry.tables.block.GenericTableBlock;
import modernmods.modernfoundry.tables.block.ScorchedAnvilBlock;
import modernmods.modernfoundry.tables.block.TinkerStationBlock;
import modernmods.modernfoundry.tables.block.TinkersAnvilBlock;
import modernmods.modernfoundry.tables.block.TinkersChestBlock;
import modernmods.modernfoundry.tables.block.entity.chest.CastChestBlockEntity;
import modernmods.modernfoundry.tables.block.entity.chest.PartChestBlockEntity;
import modernmods.modernfoundry.tables.block.entity.chest.TinkersChestBlockEntity;
import modernmods.modernfoundry.tables.block.entity.table.CraftingStationBlockEntity;
import modernmods.modernfoundry.tables.block.entity.table.ModifierWorktableBlockEntity;
import modernmods.modernfoundry.tables.block.entity.table.PartBuilderBlockEntity;
import modernmods.modernfoundry.tables.block.entity.table.TinkerStationBlockEntity;
import modernmods.modernfoundry.tables.item.AnvilBlockItem;
import modernmods.modernfoundry.tables.item.TinkersChestBlockItem;
import modernmods.modernfoundry.tables.menu.CraftingStationContainerMenu;
import modernmods.modernfoundry.tables.menu.ModifierWorktableContainerMenu;
import modernmods.modernfoundry.tables.menu.PartBuilderContainerMenu;
import modernmods.modernfoundry.tables.menu.TinkerChestContainerMenu;
import modernmods.modernfoundry.tables.menu.TinkerStationContainerMenu;
import modernmods.modernfoundry.tables.recipe.CraftingTableRepairKitRecipe;
import modernmods.modernfoundry.tables.recipe.PartBuilderToolRecycle;
import modernmods.modernfoundry.tables.recipe.TinkerStationDamagingRecipe;
import modernmods.modernfoundry.tables.recipe.TinkerStationPartSwapping;
import modernmods.modernfoundry.tables.recipe.TinkerStationRepairRecipe;
import modernmods.modernfoundry.tools.TinkerToolParts;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Handles all the table for tool creation
 */
@SuppressWarnings("unused")
public final class TinkerTables extends TinkerModule {
  /** Creative tab for general items, or those that lack another tab */
  public static final DeferredHolder<? super CreativeModeTab, CreativeModeTab> tabTables = CREATIVE_TABS.register(
    "tables", () -> CreativeModeTab.builder().title(TConstruct.makeTranslation("itemGroup", "tables"))
                                   .icon(() -> new ItemStack(TinkerTables.craftingStation))
                                   .displayItems(TinkerTables::addTabItems)
                                   .withTabsBefore(TinkerCommons.tabGeneral.getId())
                                   .build());
  /*
   * Blocks
   */
  public static final ItemObject<TableBlock> craftingStation, tinkerStation, partBuilder, tinkersChest, partChest;
  static {
    Block.Properties WOOD_TABLE = builder(MapColor.WOOD, SoundType.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F, 5.0F).noOcclusion();
    craftingStation = BLOCKS.register("crafting_station", () -> new CraftingStationBlock(WOOD_TABLE), RETEXTURED_BLOCK_ITEM);
    tinkerStation = BLOCKS.register("tinker_station", () -> new TinkerStationBlock(WOOD_TABLE, 4), RETEXTURED_BLOCK_ITEM);
    partBuilder = BLOCKS.register("part_builder", () -> new GenericTableBlock(WOOD_TABLE, PartBuilderBlockEntity::new), RETEXTURED_BLOCK_ITEM);
    tinkersChest = BLOCKS.register("tinkers_chest", () -> new TinkersChestBlock(WOOD_TABLE, TinkersChestBlockEntity::new, true), block -> new TinkersChestBlockItem(block, ITEM_PROPS));
    partChest = BLOCKS.register("part_chest", () -> new ChestBlock(WOOD_TABLE, PartChestBlockEntity::new, true), BLOCK_ITEM);
  }

  public static final ItemObject<TableBlock> castChest, modifierWorktable;
  static {
    Block.Properties STONE_TABLE = builder(MapColor.COLOR_GRAY, SoundType.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F).noOcclusion();
    castChest = BLOCKS.register("cast_chest", () -> new ChestBlock(STONE_TABLE, CastChestBlockEntity::new, false), BLOCK_ITEM);
    modifierWorktable = BLOCKS.register("modifier_worktable", () -> new GenericTableBlock(STONE_TABLE, ModifierWorktableBlockEntity::new), RETEXTURED_BLOCK_ITEM);
  }

  public static final ItemObject<TableBlock> tinkersAnvil, scorchedAnvil;
  static {
    Block.Properties METAL_TABLE = builder(MapColor.COLOR_GRAY, SoundType.ANVIL).pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).noOcclusion();
    Function<Block, BlockItem> blockItem = block -> new AnvilBlockItem(block, ITEM_PROPS, TinkerToolParts.fakeStorageBlockItem, TinkerTags.Materials.COMPATABILITY_ALLOYS);
    tinkersAnvil = BLOCKS.register("tinkers_anvil", () -> new TinkersAnvilBlock(METAL_TABLE, 6), blockItem);
    scorchedAnvil = BLOCKS.register("scorched_anvil", () -> new ScorchedAnvilBlock(METAL_TABLE, 6), blockItem);
  }
  /*
   * Items
   */
  public static final ItemObject<Item> pattern = ITEMS.register("pattern", ITEM_PROPS);

  /*
   * Tile entites
   */
  public static final DeferredHolder<? super BlockEntityType<CraftingStationBlockEntity>, BlockEntityType<CraftingStationBlockEntity>> craftingStationTile = BLOCK_ENTITIES.register("crafting_station", CraftingStationBlockEntity::new, craftingStation);
  public static final DeferredHolder<? super BlockEntityType<TinkerStationBlockEntity>, BlockEntityType<TinkerStationBlockEntity>> tinkerStationTile = BLOCK_ENTITIES.register("tinker_station", TinkerStationBlockEntity::new, builder ->
    builder.add(tinkerStation.get(), tinkersAnvil.get(), scorchedAnvil.get()));
  public static final DeferredHolder<? super BlockEntityType<PartBuilderBlockEntity>, BlockEntityType<PartBuilderBlockEntity>> partBuilderTile = BLOCK_ENTITIES.register("part_builder", PartBuilderBlockEntity::new, partBuilder);
  public static final DeferredHolder<? super BlockEntityType<ModifierWorktableBlockEntity>, BlockEntityType<ModifierWorktableBlockEntity>> modifierWorktableTile = BLOCK_ENTITIES.register("modifier_worktable", ModifierWorktableBlockEntity::new, modifierWorktable);
  // legacy name as tile entities cannot be remapped
  public static final DeferredHolder<? super BlockEntityType<TinkersChestBlockEntity>, BlockEntityType<TinkersChestBlockEntity>> tinkersChestTile = BLOCK_ENTITIES.register("modifier_chest", TinkersChestBlockEntity::new, tinkersChest);
  public static final DeferredHolder<? super BlockEntityType<PartChestBlockEntity>, BlockEntityType<PartChestBlockEntity>> partChestTile = BLOCK_ENTITIES.register("part_chest", PartChestBlockEntity::new, partChest);
  public static final DeferredHolder<? super BlockEntityType<CastChestBlockEntity>, BlockEntityType<CastChestBlockEntity>> castChestTile = BLOCK_ENTITIES.register("cast_chest", CastChestBlockEntity::new, castChest);

  /*
   * Containers
   */
  public static final DeferredHolder<? super MenuType<CraftingStationContainerMenu>, MenuType<CraftingStationContainerMenu>> craftingStationContainer = MENUS.register("crafting_station", CraftingStationContainerMenu::new);
  public static final DeferredHolder<? super MenuType<TinkerStationContainerMenu>, MenuType<TinkerStationContainerMenu>> tinkerStationContainer = MENUS.register("tinker_station", TinkerStationContainerMenu::new);
  public static final DeferredHolder<? super MenuType<PartBuilderContainerMenu>, MenuType<PartBuilderContainerMenu>> partBuilderContainer = MENUS.register("part_builder", PartBuilderContainerMenu::new);
  public static final DeferredHolder<? super MenuType<ModifierWorktableContainerMenu>, MenuType<ModifierWorktableContainerMenu>> modifierWorktableContainer = MENUS.register("modifier_worktable", ModifierWorktableContainerMenu::new);
  public static final DeferredHolder<? super MenuType<TinkerChestContainerMenu>, MenuType<TinkerChestContainerMenu>> tinkerChestContainer = MENUS.register("tinker_chest", TinkerChestContainerMenu::new);

  /*
   * Recipes
   */
  public static final DeferredHolder<? super RecipeSerializer<MaterialRecipe>, RecipeSerializer<MaterialRecipe>> materialRecipeSerializer = RECIPE_SERIALIZERS.register("material", () -> LoadableRecipeSerializer.of(MaterialRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ToolBuildingRecipe>, RecipeSerializer<ToolBuildingRecipe>> toolBuildingRecipeSerializer = RECIPE_SERIALIZERS.register("tool_building", () -> LoadableRecipeSerializer.of(ToolBuildingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<TinkerStationPartSwapping>, RecipeSerializer<TinkerStationPartSwapping>> tinkerStationPartSwappingSerializer = RECIPE_SERIALIZERS.register("tinker_station_part_swapping", () -> LoadableRecipeSerializer.of(TinkerStationPartSwapping.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<TinkerStationDamagingRecipe>, RecipeSerializer<TinkerStationDamagingRecipe>> tinkerStationDamagingSerializer = RECIPE_SERIALIZERS.register("tinker_station_damaging", () -> LoadableRecipeSerializer.of(TinkerStationDamagingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<FixedMaterialSwappingRecipe>, RecipeSerializer<FixedMaterialSwappingRecipe>> fixedMaterialSwapping = RECIPE_SERIALIZERS.register("fixed_material_swapping", () -> LoadableRecipeSerializer.of(FixedMaterialSwappingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<PartSwappingOverrideRecipe>, RecipeSerializer<PartSwappingOverrideRecipe>> partSwappingOverride = RECIPE_SERIALIZERS.register("part_swapping_override", () -> LoadableRecipeSerializer.of(PartSwappingOverrideRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ToolMaterialSwappingRecipe>, RecipeSerializer<ToolMaterialSwappingRecipe>> toolMaterialSwapping = RECIPE_SERIALIZERS.register("tool_material_swapping", () -> LoadableRecipeSerializer.of(ToolMaterialSwappingRecipe.LOADER));
  @Deprecated
  public static final DeferredHolder<? super RecipeSerializer<ShapedMaterialRecipe>, RecipeSerializer<ShapedMaterialRecipe>> shapedMaterialRecipeSerializer = RECIPE_SERIALIZERS.register("crafting_shaped_material", () -> new ShapedMaterialRecipe.Serializer().serializer());
  public static final DeferredHolder<? super RecipeSerializer<ShapedMaterialsRecipe>, RecipeSerializer<ShapedMaterialsRecipe>> shapedMaterialsRecipeSerializer = RECIPE_SERIALIZERS.register("crafting_shaped_materials", () -> new ShapedMaterialsRecipe.Serializer().serializer());
  public static final DeferredHolder<? super RecipeSerializer<ShapelessMaterialsRecipe>, RecipeSerializer<ShapelessMaterialsRecipe>> shapelessMaterialsRecipeSerializer = RECIPE_SERIALIZERS.register("crafting_shapeless_materials", () -> new ShapelessMaterialsRecipe.Serializer().serializer());
  // part builder
  public static final DeferredHolder<? super RecipeSerializer<PartRecipe>, RecipeSerializer<PartRecipe>> partRecipeSerializer = RECIPE_SERIALIZERS.register("part_builder", () -> LoadableRecipeSerializer.of(PartRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ItemPartRecipe>, RecipeSerializer<ItemPartRecipe>> itemPartBuilderSerializer = RECIPE_SERIALIZERS.register("item_part_builder", () -> LoadableRecipeSerializer.of(ItemPartRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<PartBuilderToolRecycle>, RecipeSerializer<PartBuilderToolRecycle>> partBuilderToolRecycling = RECIPE_SERIALIZERS.register("part_builder_tool_recycling", () -> LoadableRecipeSerializer.of(PartBuilderToolRecycle.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<PartBuilderRecycle>, RecipeSerializer<PartBuilderRecycle>> partBuilderDamageableRecycling = RECIPE_SERIALIZERS.register("part_builder_recycling", () -> LoadableRecipeSerializer.of(PartBuilderRecycle.LOADER));
  // repair - standard
  public static final DeferredHolder<? super RecipeSerializer<TinkerStationRepairRecipe>, RecipeSerializer<TinkerStationRepairRecipe>> tinkerStationRepairSerializer = RECIPE_SERIALIZERS.register("tinker_station_repair", () -> new SimpleRecipeSerializer<>(TinkerStationRepairRecipe::new).serializer());
  public static final DeferredHolder<? super RecipeSerializer<CraftingTableRepairKitRecipe>, RecipeSerializer<CraftingTableRepairKitRecipe>> craftingTableRepairSerializer = RECIPE_SERIALIZERS.register("crafting_table_repair", () -> new SimpleRecipeSerializer<>(CraftingTableRepairKitRecipe::new).serializer());

  @SubscribeEvent
  void commonSetup(final FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
      StationSlotLayoutLoader loader = StationSlotLayoutLoader.getInstance();
      loader.registerRequiredLayout(tinkerStation.getId());
      loader.registerRequiredLayout(tinkersAnvil.getId());
      loader.registerRequiredLayout(scorchedAnvil.getId());
    });
  }

  /** Adds all relevant items to the creative tab, called in the general tab */
  private static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
    output.accept(pattern);

    // add one of each standard table
    output.accept(craftingStation);
    output.accept(partBuilder);
    output.accept(tinkerStation);
    // if showing all anvil variants, skip them in search at this first stage
    output.accept(tinkersAnvil);
    output.accept(scorchedAnvil);
    output.accept(modifierWorktable);

    // chests, have less variants so go first
    output.accept(tinkersChest);
    output.accept(partChest);
    output.accept(castChest);

    // table variants at the end as there may be a lot
    Predicate<ItemStack> variants = stack -> {
      output.accept(stack);
      return false;
    };
    // crafting tables
    // add crafting station with the default variant, its nice
    RetexturedHelper.addTagVariants(variants, craftingStation, ItemTags.LOGS);
    // rest the default variant is the same as oak
    RetexturedHelper.addTagVariants(variants, partBuilder, ItemTags.PLANKS);
    RetexturedHelper.addTagVariants(variants, tinkerStation, ItemTags.PLANKS);
    // anvil variants use their own config prop as the variants are less obvious
    Consumer<ItemStack> consumer = output::accept;
    ((IMaterialItem) tinkersAnvil.asItem()).addVariants(consumer, "");
    ((IMaterialItem) scorchedAnvil.asItem()).addVariants(consumer, "");
    RetexturedHelper.addTagVariants(variants, modifierWorktable, TinkerTags.Items.WORKSTATION_ROCK);
  }
}
