package modernmods.modernfoundry.shared;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import modernmods.modernfoundry.compat.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import modernmods.mantle.compat.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.mantle.data.predicate.block.BlockPredicate;
import modernmods.mantle.data.predicate.damage.DamageSourcePredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.mantle.data.predicate.item.ItemPredicate;
import modernmods.mantle.item.EdibleItem;
import modernmods.mantle.registration.object.EnumObject;
import modernmods.mantle.registration.object.ItemObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerModule;
import modernmods.modernfoundry.common.json.BlockOrEntityCondition;
import modernmods.modernfoundry.common.json.ConfigEnabledCondition;
import modernmods.modernfoundry.common.recipe.RecipeCacheInvalidator;
import modernmods.modernfoundry.gadgets.TinkerGadgets;
import modernmods.modernfoundry.library.json.condition.TagDifferencePresentCondition;
import modernmods.modernfoundry.library.json.condition.TagIntersectionPresentCondition;
import modernmods.modernfoundry.library.json.condition.TagNotEmptyCondition;
import modernmods.modernfoundry.library.json.loot.HasLootContextSetCondition;
import modernmods.modernfoundry.library.json.loot.TagPreferenceLootEntry;
import modernmods.modernfoundry.library.json.predicate.tool.ToolStackItemPredicate;
import modernmods.modernfoundry.library.json.predicate.BlockAtFeetEntityPredicate;
import modernmods.modernfoundry.library.json.predicate.BlockVariableRangePredicate;
import modernmods.modernfoundry.library.json.predicate.EntityVariableRangePredicate;
import modernmods.modernfoundry.library.json.predicate.HarvestTierPredicate;
import modernmods.modernfoundry.library.json.predicate.HasMobEffectPredicate;
import modernmods.modernfoundry.library.json.predicate.TinkerPredicate;
import modernmods.modernfoundry.library.recipe.ingredient.BlockTagIngredient;
import modernmods.modernfoundry.library.recipe.ingredient.NoContainerIngredient;
import modernmods.modernfoundry.library.utils.SlimeBounceHandler;
import modernmods.modernfoundry.shared.block.BetterPaneBlock;
import modernmods.modernfoundry.shared.block.ClearGlassPaneBlock;
import modernmods.modernfoundry.shared.block.ClearStainedGlassBlock;
import modernmods.modernfoundry.shared.block.ClearStainedGlassBlock.GlassColor;
import modernmods.modernfoundry.shared.block.ClearStainedGlassPaneBlock;
import modernmods.modernfoundry.shared.block.GlowBlock;
import modernmods.modernfoundry.shared.block.PlatformBlock;
import modernmods.modernfoundry.shared.block.SlimeType;
import modernmods.modernfoundry.shared.block.SoulGlassBlock;
import modernmods.modernfoundry.shared.block.SoulGlassPaneBlock;
import modernmods.modernfoundry.shared.block.WaxedPlatformBlock;
import modernmods.modernfoundry.shared.block.WeatheringPlatformBlock;
import modernmods.modernfoundry.shared.command.TConstructCommand;
import modernmods.modernfoundry.shared.inventory.BlockContainerOpenedTrigger;
import modernmods.modernfoundry.shared.item.CheeseBlockItem;
import modernmods.modernfoundry.shared.item.CheeseItem;
import modernmods.modernfoundry.shared.item.TinkerBookItem;
import modernmods.modernfoundry.shared.item.TinkerBookItem.BookType;
import modernmods.modernfoundry.shared.particle.FluidParticleData;
import modernmods.modernfoundry.tools.TinkerModifiers;

import static modernmods.modernfoundry.TConstruct.getResource;

/**
 * Contains items and blocks and stuff that is shared by multiple modules, but might be required individually
 */
@SuppressWarnings("unused")
public final class TinkerCommons extends TinkerModule {
  /** Creative tab for general items, or those that lack another tab */
  public static final DeferredHolder<? super CreativeModeTab, CreativeModeTab> tabGeneral = CREATIVE_TABS.register(
    "general", () -> CreativeModeTab.builder().title(TConstruct.makeTranslation("itemGroup", "general"))
                                    .icon(() -> new ItemStack(TinkerCommons.materialsAndYou))
                                    .displayItems(TinkerCommons::addTabItems)
                                    .build());

  /*
   * Blocks
   */
  public static final ItemObject<GlowBlock> glowBlock = BLOCKS.register("glow", () -> new GlowBlock(builder(MapColor.NONE, SoundType.WOOL).noCollision().pushReaction(PushReaction.DESTROY).replaceable().strength(0.0F).lightLevel(s -> 14).noOcclusion()), BLOCK_ITEM);
  /**
   * @deprecated Use {@link #glowBlock}
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<? super GlowBlock, GlowBlock> glow = (DeferredHolder) DeferredHolder.create(net.minecraft.core.registries.Registries.BLOCK, glowBlock.getId());
  // glass
  public static final ItemObject<GlassBlock> clearGlass = BLOCKS.register("clear_glass", () -> new GlassBlock(glassBuilder(MapColor.NONE)), BLOCK_ITEM);
  public static final ItemObject<TintedGlassBlock> clearTintedGlass = BLOCKS.register("clear_tinted_glass", () -> new TintedGlassBlock(glassBuilder(MapColor.COLOR_GRAY).noOcclusion().isValidSpawn((state, level, pos, entity) -> false).isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)), BLOCK_ITEM);
  public static final ItemObject<ClearGlassPaneBlock> clearGlassPane = BLOCKS.register("clear_glass_pane", () -> new ClearGlassPaneBlock(glassBuilder(MapColor.NONE)), BLOCK_ITEM);
  public static final EnumObject<GlassColor,ClearStainedGlassBlock> clearStainedGlass = BLOCKS.registerEnum(GlassColor.values(), "clear_stained_glass", (color) -> new ClearStainedGlassBlock(glassBuilder(color.getDye().getMapColor()), color), BLOCK_ITEM);
  public static final EnumObject<GlassColor,ClearStainedGlassPaneBlock> clearStainedGlassPane = BLOCKS.registerEnum(GlassColor.values(), "clear_stained_glass_pane", (color) -> new ClearStainedGlassPaneBlock(glassBuilder(color.getDye().getMapColor()), color), BLOCK_ITEM);
  public static final ItemObject<GlassBlock> soulGlass = BLOCKS.register("soul_glass", () -> new SoulGlassBlock(glassBuilder(MapColor.COLOR_BROWN).speedFactor(0.2F).noCollision().isViewBlocking((state, getter, pos) -> true)), TOOLTIP_BLOCK_ITEM);
  public static final ItemObject<ClearGlassPaneBlock> soulGlassPane = BLOCKS.register("soul_glass_pane", () -> new SoulGlassPaneBlock(glassBuilder(MapColor.COLOR_BROWN).speedFactor(0.2F)), TOOLTIP_BLOCK_ITEM);
  // panes
  public static final ItemObject<IronBarsBlock> goldBars = BLOCKS.register("gold_bars", () -> new IronBarsBlock(builder(MapColor.NONE, SoundType.METAL).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), TOOLTIP_BLOCK_ITEM);
  public static final ItemObject<BetterPaneBlock> obsidianPane = BLOCKS.register("obsidian_pane", () -> new BetterPaneBlock(builder(MapColor.COLOR_BLACK, SoundType.STONE).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().strength(25.0F, 400.0F)), BLOCK_ITEM);
  // platforms
  public static final ItemObject<PlatformBlock> goldPlatform = BLOCKS.register("gold_platform", () -> new PlatformBlock(builder(MapColor.GOLD, SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), TOOLTIP_BLOCK_ITEM);
  public static final ItemObject<PlatformBlock> ironPlatform = BLOCKS.register("iron_platform", () -> new PlatformBlock(builder(MapColor.METAL, SoundType.COPPER).requiresCorrectToolForDrops().strength(5.0F, 6.0F).noOcclusion()), BLOCK_ITEM);
  public static final ItemObject<PlatformBlock> cobaltPlatform = BLOCKS.register("cobalt_platform", () -> new PlatformBlock(builder(MapColor.COLOR_BLUE, SoundType.COPPER).requiresCorrectToolForDrops().strength(5.0f).noOcclusion()), BLOCK_ITEM);
  public static final EnumObject<WeatherState,PlatformBlock> copperPlatform = new EnumObject.Builder<WeatherState,PlatformBlock>(WeatherState.class)
    .put(WeatherState.UNAFFECTED, BLOCKS.register("copper_platform",           () -> new WeatheringPlatformBlock(WeatherState.UNAFFECTED, builder(MapColor.COLOR_ORANGE,          SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .put(WeatherState.EXPOSED,    BLOCKS.register("exposed_copper_platform",   () -> new WeatheringPlatformBlock(WeatherState.EXPOSED,    builder(MapColor.TERRACOTTA_LIGHT_GRAY, SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .put(WeatherState.WEATHERED,  BLOCKS.register("weathered_copper_platform", () -> new WeatheringPlatformBlock(WeatherState.WEATHERED,  builder(MapColor.WARPED_STEM,           SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .put(WeatherState.OXIDIZED,   BLOCKS.register("oxidized_copper_platform",  () -> new WeatheringPlatformBlock(WeatherState.OXIDIZED,   builder(MapColor.WARPED_NYLIUM,         SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .build();
  public static final EnumObject<WeatherState,PlatformBlock> waxedCopperPlatform = new EnumObject.Builder<WeatherState,PlatformBlock>(WeatherState.class)
    .put(WeatherState.UNAFFECTED, BLOCKS.register("waxed_copper_platform",           () -> new WaxedPlatformBlock(WeatherState.UNAFFECTED, builder(MapColor.COLOR_ORANGE,          SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .put(WeatherState.EXPOSED,    BLOCKS.register("waxed_exposed_copper_platform",   () -> new WaxedPlatformBlock(WeatherState.EXPOSED,    builder(MapColor.TERRACOTTA_LIGHT_GRAY, SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .put(WeatherState.WEATHERED,  BLOCKS.register("waxed_weathered_copper_platform", () -> new WaxedPlatformBlock(WeatherState.WEATHERED,  builder(MapColor.WARPED_STEM,           SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .put(WeatherState.OXIDIZED,   BLOCKS.register("waxed_oxidized_copper_platform",  () -> new WaxedPlatformBlock(WeatherState.OXIDIZED,   builder(MapColor.WARPED_NYLIUM,         SoundType.COPPER).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()), BLOCK_ITEM))
    .build();


  /*
   * Items
   */
  public static final ItemObject<EdibleItem> bacon = ITEMS.register("bacon", () -> new EdibleItem(TinkerFood.BACON));
  public static final ItemObject<EdibleItem> jeweledApple = ITEMS.register("jeweled_apple", () -> new EdibleItem(new Properties().food(TinkerFood.JEWELED_APPLE, TinkerFood.JEWELED_APPLE_CONSUMABLE)));
  public static final ItemObject<Item> cheeseIngot = ITEMS.register("cheese_ingot", () -> new CheeseItem(new Properties().food(TinkerFood.CHEESE)));
  public static final ItemObject<Block> cheeseBlock = BLOCKS.register("cheese_block", () -> new HalfTransparentBlock(builder(MapColor.COLOR_YELLOW, SoundType.HONEY_BLOCK).strength(1.5F, 3.0F).speedFactor(0.4F).jumpFactor(0.5F).noOcclusion()), block -> new CheeseBlockItem(block, new Properties().food(TinkerFood.CHEESE)));

  public static final ItemObject<TinkerBookItem> materialsAndYou  = ITEMS.register("materials_and_you", () -> new TinkerBookItem(UNSTACKABLE_PROPS, BookType.MATERIALS_AND_YOU));
  public static final ItemObject<TinkerBookItem> punySmelting     = ITEMS.register("puny_smelting",     () -> new TinkerBookItem(UNSTACKABLE_PROPS, BookType.PUNY_SMELTING));
  public static final ItemObject<TinkerBookItem> mightySmelting   = ITEMS.register("mighty_smelting",   () -> new TinkerBookItem(UNSTACKABLE_PROPS, BookType.MIGHTY_SMELTING));
  public static final ItemObject<TinkerBookItem> tinkersGadgetry  = ITEMS.register("tinkers_gadgetry",  () -> new TinkerBookItem(UNSTACKABLE_PROPS, BookType.TINKERS_GADGETRY));
  public static final ItemObject<TinkerBookItem> fantasticFoundry = ITEMS.register("fantastic_foundry", () -> new TinkerBookItem(UNSTACKABLE_PROPS, BookType.FANTASTIC_FOUNDRY));
  public static final ItemObject<TinkerBookItem> encyclopedia     = ITEMS.register("encyclopedia",      () -> new TinkerBookItem(UNSTACKABLE_PROPS, BookType.ENCYCLOPEDIA));

  public static final DeferredHolder<? super ParticleType<FluidParticleData>, ParticleType<FluidParticleData>> fluidParticle = PARTICLE_TYPES.register("fluid", FluidParticleData.Type::new);
  public static final DeferredHolder<? super DataComponentPredicate.Type<ToolStackItemPredicate>, DataComponentPredicate.Type<ToolStackItemPredicate>> toolStackItemPredicate = ITEM_SUB_PREDICATES.register("tool_stack", () -> new DataComponentPredicate.ConcreteType<>(ToolStackItemPredicate.CODEC));

  /* Loot conditions */
  public static final DeferredHolder<MapCodec<? extends LootItemCondition>, ? extends MapCodec<? extends LootItemCondition>> lootConfig = LOOT_CONDITIONS.register(ConfigEnabledCondition.ID.getPath(), () -> ConfigEnabledCondition.CODEC);
  public static final DeferredHolder<MapCodec<? extends LootItemCondition>, ? extends MapCodec<? extends LootItemCondition>> lootBlockOrEntity = LOOT_CONDITIONS.register("block_or_entity", () -> BlockOrEntityCondition.CODEC);
  public static final DeferredHolder<MapCodec<? extends LootItemCondition>, ? extends MapCodec<? extends LootItemCondition>> hasLootContextSet = LOOT_CONDITIONS.register("has_context_set", () -> HasLootContextSetCondition.CODEC);
  /** @deprecated use {@link modernmods.mantle.loot.MantleLoot#TAG_FILLED} */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MapCodec<? extends LootItemCondition>, ? extends MapCodec<? extends LootItemCondition>> lootTagNotEmptyCondition = LOOT_CONDITIONS.register("tag_not_empty", () -> TagNotEmptyCondition.CODEC);
  /** @deprecated use {@link modernmods.mantle.loot.MantleLoot#TAG_PREFERENCE} */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MapCodec<? extends LootPoolEntryContainer>, ? extends MapCodec<? extends LootPoolEntryContainer>> lootTagPreference = LOOT_ENTRIES.register("tag_preference", () -> TagPreferenceLootEntry.CODEC);
  public static final DeferredHolder<? super IngredientType<NoContainerIngredient>, IngredientType<NoContainerIngredient>> noContainerIngredient = INGREDIENT_TYPES.register("no_container", () -> new IngredientType<>(NoContainerIngredient.Serializer.INSTANCE.codec(), NoContainerIngredient.Serializer.INSTANCE.streamCodec()));
  public static final DeferredHolder<? super IngredientType<BlockTagIngredient>, IngredientType<BlockTagIngredient>> blockTagIngredient = INGREDIENT_TYPES.register("block_tag", () -> new IngredientType<>(BlockTagIngredient.Serializer.INSTANCE.codec(), BlockTagIngredient.Serializer.INSTANCE.streamCodec()));

  /* Recipe conditions */
  public static final DeferredHolder<? super com.mojang.serialization.MapCodec<? extends ICondition>, com.mojang.serialization.MapCodec<? extends ICondition>> configCondition = CONDITION_CODECS.register(ConfigEnabledCondition.ID.getPath(), () -> ConfigEnabledCondition.CODEC);
  @SuppressWarnings("removal")
  public static final DeferredHolder<? super com.mojang.serialization.MapCodec<? extends ICondition>, com.mojang.serialization.MapCodec<? extends ICondition>> tagIntersectionPresentCondition = CONDITION_CODECS.register("tag_intersection_present", () -> TagIntersectionPresentCondition.CODEC);
  @SuppressWarnings("removal")
  public static final DeferredHolder<? super com.mojang.serialization.MapCodec<? extends ICondition>, com.mojang.serialization.MapCodec<? extends ICondition>> tagDifferencePresentCondition = CONDITION_CODECS.register("tag_difference_present", () -> TagDifferencePresentCondition.CODEC);
  @SuppressWarnings("removal")
  public static final DeferredHolder<? super com.mojang.serialization.MapCodec<? extends ICondition>, com.mojang.serialization.MapCodec<? extends ICondition>> tagNotEmptyCondition = CONDITION_CODECS.register("tag_not_empty", () -> TagNotEmptyCondition.CODEC);

  /* Slime Balls are edible, believe it or not */
  public static final EnumObject<SlimeType, Item> slimeball = new EnumObject.Builder<SlimeType, Item>(SlimeType.class)
    .put(SlimeType.EARTH, () -> Items.SLIME_BALL)
    .putAll(ITEMS.registerEnum(SlimeType.TINKER, "slime_ball", type -> new Item(ITEM_PROPS)))
    .build();

  public static final BlockContainerOpenedTrigger CONTAINER_OPENED_TRIGGER = new BlockContainerOpenedTrigger();

  public TinkerCommons() {
    TConstructCommand.init();
    NeoForge.EVENT_BUS.addListener(RecipeCacheInvalidator::onReloadListenerReload);
  }

  @SubscribeEvent
  void commonSetupEvent(FMLCommonSetupEvent event) {
    SlimeBounceHandler.init();
  }

  @SuppressWarnings("removal")
  @SubscribeEvent
  void registerRecipeSerializers(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      CriteriaTriggers.register(TConstruct.getResource("block_container_opened").toString(), CONTAINER_OPENED_TRIGGER);
      // mantle
      DamageSourcePredicate.LOADER.register(getResource("direct"), TinkerPredicate.DIRECT_DAMAGE.getLoader());
      // entity
      LivingEntityPredicate.LOADER.register(getResource("airborne"), TinkerPredicate.AIRBORNE.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("targeting_block"), TinkerPredicate.TARGETING_BLOCK.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("full_health"), TinkerPredicate.FULL_HEALTH.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("variable_range"), EntityVariableRangePredicate.LOADER);
      LivingEntityPredicate.LOADER.register(getResource("has_effect"), HasMobEffectPredicate.LOADER);
      LivingEntityPredicate.LOADER.register(getResource("block_at_feet"), BlockAtFeetEntityPredicate.LOADER);
      // item
      ItemPredicate.LOADER.register(getResource("arrow"), TinkerPredicate.ARROW.getLoader());
      ItemPredicate.LOADER.register(getResource("bucket"), TinkerPredicate.BUCKET.getLoader());
      ItemPredicate.LOADER.register(getResource("map"), TinkerPredicate.MAP.getLoader());
      ItemPredicate.LOADER.register(getResource("can_melt"), TinkerPredicate.CAN_MELT_ITEM.getLoader());
      ItemPredicate.LOADER.register(getResource("castable"), TinkerPredicate.CASTABLE.getLoader());
      // block
      BlockPredicate.LOADER.register(getResource("blocks_motion"), TinkerPredicate.BLOCKS_MOTION.getLoader());
      BlockPredicate.LOADER.register(getResource("can_be_replaced"), TinkerPredicate.CAN_BE_REPLACED.getLoader());
      BlockPredicate.LOADER.register(getResource("bush"), TinkerPredicate.BUSH.getLoader());
      BlockPredicate.LOADER.register(getResource("can_melt"), TinkerPredicate.CAN_MELT_BLOCK.getLoader());
      BlockPredicate.LOADER.register(getResource("harvest_tier"), HarvestTierPredicate.LOADER);
      BlockPredicate.LOADER.register(getResource("variable_range"), BlockVariableRangePredicate.LOADER);
    }
  }

  /** Adds all relevant items to the creative tab */
  private static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
    // books
    output.accept(materialsAndYou);
    output.accept(punySmelting);
    output.accept(mightySmelting);
    output.accept(tinkersGadgetry);
    output.accept(fantasticFoundry);
    output.accept(encyclopedia);

    // food
    output.accept(bacon);
    output.accept(jeweledApple);
    output.accept(cheeseIngot);
    output.accept(cheeseBlock);

    // glass
    output.accept(clearGlass);
    accept(output, clearStainedGlass);
    output.accept(clearTintedGlass);
    output.accept(soulGlass);
    output.accept(clearGlassPane);
    accept(output, clearStainedGlassPane);
    output.accept(soulGlassPane);
    // bars
    output.accept(goldBars);
    output.accept(obsidianPane);
    // platforms
    accept(output, copperPlatform);
    accept(output, waxedCopperPlatform);
    output.accept(ironPlatform);
    output.accept(goldPlatform);
    output.accept(cobaltPlatform);

    // slimeballs are in world

    TinkerGadgets.addTabItems(itemDisplayParameters, output);
    TinkerMaterials.addTabItems(itemDisplayParameters, output);
    TinkerModifiers.addTabItems(itemDisplayParameters, output);
  }
}
