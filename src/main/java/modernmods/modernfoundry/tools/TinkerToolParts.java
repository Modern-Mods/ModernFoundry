package modernmods.modernfoundry.tools;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.mantle.registration.object.EnumObject;
import modernmods.mantle.registration.object.ItemObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerModule;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.loot.ToolPartLootEntry;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.tools.definition.ModifiableArmorMaterial;
import modernmods.modernfoundry.library.tools.helper.ToolBuildHandler;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.library.tools.part.IRepairKitItem;
import modernmods.modernfoundry.library.tools.part.ToolPartItem;
import modernmods.modernfoundry.library.tools.part.block.MaterialBlock;
import modernmods.modernfoundry.library.tools.part.block.MaterialBlockEntity;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.tools.item.FakeIngotItem;
import modernmods.modernfoundry.tools.item.FakeStorageBlockItem;
import modernmods.modernfoundry.tools.item.ModifierCrystalItem;
import modernmods.modernfoundry.tools.item.RepairKitItem;
import modernmods.modernfoundry.tools.stats.GripMaterialStats;
import modernmods.modernfoundry.tools.stats.HandleMaterialStats;
import modernmods.modernfoundry.tools.stats.HeadMaterialStats;
import modernmods.modernfoundry.tools.stats.LimbMaterialStats;
import modernmods.modernfoundry.tools.stats.PlatingMaterialStats;
import modernmods.modernfoundry.tools.stats.RepairStats;
import modernmods.modernfoundry.tools.stats.StatlessMaterialStats;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TinkerToolParts extends TinkerModule {
  /** Tab for all tool parts or tool components with many variants */
  public static final DeferredHolder<? super CreativeModeTab, CreativeModeTab> tabToolParts = CREATIVE_TABS.register(
    "tool_parts", () -> CreativeModeTab.builder().title(TConstruct.makeTranslation("itemGroup", "tool_parts"))
                                       .icon(() -> {
                                         MaterialVariantId material;
                                         if (MaterialRegistry.isFullyLoaded()) {
                                           material = ToolBuildHandler.RANDOM.getMaterial(HeadMaterialStats.ID, RandomSource.create());
                                         } else {
                                           material = ToolBuildHandler.getRenderMaterial(0);
                                         }
                                         return TinkerToolParts.pickHead.get().withMaterialForDisplay(material);
                                       })
                                       .displayItems(TinkerToolParts::addTabItems)
                                       .withTabsBefore(TinkerTools.tabTools.getId())
                                       .withSearchBar()
                                       .build());

  // repair kits
  public static final ItemObject<RepairKitItem> repairKit = ITEMS.register("repair_kit", () -> new RepairKitItem(ITEM_PROPS));
  /** Fake ingot tool part for the sake of compat materials that lack an ingot form. Mainly used for compat alloys such as bronze which can be activated by their components. */
  public static final ItemObject<FakeIngotItem> fakeIngot = ITEMS.register("fake_ingot", () -> new FakeIngotItem(ITEM_PROPS, 1, TinkerTags.Materials.COMPATABILITY_METALS));
  /** Fake block tool part for the sake of compat materials that lack an ingot form. Mainly used for compat alloys such as bronze which can be activated by their components. */
  public static final ItemObject<MaterialBlock> fakeStorageBlock = BLOCKS.register("fake_storage_block", () -> new MaterialBlock(metalBuilder(MapColor.COLOR_GRAY), MaterialBlockEntity::new), block -> new FakeStorageBlockItem(block, ITEM_PROPS, 9, TinkerTags.Materials.COMPATABILITY_BLOCKS));
  /** Same as {@link #fakeStorageBlock} but casted to an appropriate item interface form */
  public static final Supplier<IRepairKitItem> fakeStorageBlockItem = () -> (IRepairKitItem) fakeStorageBlock.asItem();

  // rock
  public static final ItemObject<ToolPartItem> pickHead = ITEMS.register("pick_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final ItemObject<ToolPartItem> hammerHead = ITEMS.register("hammer_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // axe
  public static final ItemObject<ToolPartItem> smallAxeHead = ITEMS.register("small_axe_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final ItemObject<ToolPartItem> broadAxeHead = ITEMS.register("broad_axe_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // blades
  public static final ItemObject<ToolPartItem> smallBlade = ITEMS.register("small_blade", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final ItemObject<ToolPartItem> broadBlade = ITEMS.register("broad_blade", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // plates
  public static final ItemObject<ToolPartItem> adzeHead = ITEMS.register("adze_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final ItemObject<ToolPartItem> largePlate = ITEMS.register("large_plate", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // bows
  public static final ItemObject<ToolPartItem> bowLimb = ITEMS.register("bow_limb", () -> new ToolPartItem(ITEM_PROPS, LimbMaterialStats.ID));
  public static final ItemObject<ToolPartItem> bowGrip = ITEMS.register("bow_grip", () -> new ToolPartItem(ITEM_PROPS, GripMaterialStats.ID));
  public static final ItemObject<ToolPartItem> bowstring = ITEMS.register("bowstring", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BOWSTRING.getIdentifier()));
  // ammo
  public static final ItemObject<ToolPartItem> arrowHead = ITEMS.register("arrow_head", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.ARROW_HEAD.getIdentifier()));
  public static final ItemObject<ToolPartItem> arrowShaft = ITEMS.register("arrow_shaft", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.ARROW_SHAFT.getIdentifier()));
  public static final ItemObject<ToolPartItem> fletching = ITEMS.register("fletching", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.FLETCHING.getIdentifier()));
  // other parts
  public static final ItemObject<ToolPartItem> toolBinding = ITEMS.register("tool_binding", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BINDING.getIdentifier()));
  public static final ItemObject<ToolPartItem> toughBinding = ITEMS.register("tough_binding", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BINDING.getIdentifier()));
  public static final ItemObject<ToolPartItem> toolHandle = ITEMS.register("tool_handle", () -> new ToolPartItem(ITEM_PROPS, HandleMaterialStats.ID));
  public static final ItemObject<ToolPartItem> toughHandle = ITEMS.register("tough_handle", () -> new ToolPartItem(ITEM_PROPS, HandleMaterialStats.ID));
  // armor
  public static final EnumObject<ArmorType,ToolPartItem> plating = ITEMS.registerEnum(ModifiableArmorMaterial.ARMOR_TYPES, "plating", type -> new ToolPartItem(ITEM_PROPS, PlatingMaterialStats.TYPES.get(type.ordinal()).getStatId()));
  public static final ItemObject<ToolPartItem> maille = ITEMS.register("maille", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.MAILLE.getIdentifier()));
  public static final ItemObject<ToolPartItem> shieldCore = ITEMS.register("shield_core", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.SHIELD_CORE.getIdentifier()));
  // slimesuit
  public static final ItemObject<ToolPartItem> ribcage = ITEMS.register("ribcage", () -> new ToolPartItem(ITEM_PROPS, RepairStats.RIBCAGE.getStatId()));
  public static final ItemObject<ToolPartItem> shell = ITEMS.register("shell", () -> new ToolPartItem(ITEM_PROPS, RepairStats.SHELL.getStatId()));
  public static final ItemObject<ToolPartItem> laces = ITEMS.register("laces", () -> new ToolPartItem(ITEM_PROPS, RepairStats.LACES.getStatId()));



  // block entities
  public static final DeferredHolder<? super BlockEntityType<MaterialBlockEntity>, BlockEntityType<MaterialBlockEntity>> materialBlock = BLOCK_ENTITIES.register("material_block", MaterialBlockEntity::new, fakeStorageBlock);

  // loot
  public static final DeferredHolder<MapCodec<? extends LootPoolEntryContainer>, ? extends MapCodec<? extends LootPoolEntryContainer>> toolPartLootEntry = LOOT_ENTRIES.register("tool_part", () -> ToolPartLootEntry.CODEC);

  /** Adds all relevant items to the creative tab */
  private static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output tab) {
    Consumer<ItemStack> output = tab::accept;
    accept(output, repairKit);
    // fake ingot is in materials tab, helps the illusion
    // small heads
    accept(output, pickHead);
    accept(output, smallAxeHead);
    accept(output, smallBlade);
    accept(output, adzeHead);
    // large heads
    accept(output, hammerHead);
    accept(output, broadAxeHead);
    accept(output, broadBlade);
    accept(output, largePlate);
    // binding and rods
    accept(output, toolHandle);
    accept(output, toolBinding);
    accept(output, toughHandle);
    accept(output, toughBinding);
    // ranged
    accept(output, bowLimb);
    accept(output, bowGrip);
    accept(output, bowstring);
    accept(output, arrowHead);
    accept(output, arrowShaft);
    accept(output, fletching);
    // plating, pair each one with the dummy plating item
    for (ArmorType type : ModifiableArmorMaterial.ARMOR_TYPES) {
      tab.accept(TinkerSmeltery.dummyPlating.get(type));
      plating.get(type).addVariants(output, "");
    }
    accept(output, maille);
    accept(output, shieldCore);
    // slimesuit
    accept(output, ribcage);
    accept(output, shell);
    accept(output, laces);

    // end with modifier crystal dynamic listing
    ModifierCrystalItem.addVariants(output);
  }

  /** Adds a tool part to the tab */
  private static void accept(Consumer<ItemStack> output, Supplier<? extends IMaterialItem> item) {
    item.get().addVariants(output, "");
  }
}
