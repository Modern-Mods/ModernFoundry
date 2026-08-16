package modernmods.modernfoundry.gadgets;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.hilt.registration.object.EnumObject;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.common.TinkerModule;
import modernmods.modernfoundry.gadgets.block.FoodCakeBlock;
import modernmods.modernfoundry.gadgets.block.FoodCakeBlock.EffectCombination;
import modernmods.modernfoundry.gadgets.block.InvertedCakeBlock;
import modernmods.modernfoundry.gadgets.block.PunjiBlock;
import modernmods.modernfoundry.gadgets.capability.PiggybackCapability;
import modernmods.modernfoundry.gadgets.entity.EFLNEntity;
import modernmods.modernfoundry.gadgets.entity.FancyItemFrameEntity;
import modernmods.modernfoundry.gadgets.entity.FrameType;
import modernmods.modernfoundry.gadgets.entity.GlowballEntity;
import modernmods.modernfoundry.gadgets.entity.shuriken.FlintShurikenEntity;
import modernmods.modernfoundry.gadgets.entity.shuriken.QuartzShurikenEntity;
import modernmods.modernfoundry.gadgets.item.EFLNItem;
import modernmods.modernfoundry.gadgets.item.FancyItemFrameItem;
import modernmods.modernfoundry.gadgets.item.GlowBallItem;
import modernmods.modernfoundry.gadgets.item.PiggyBackPackItem;
import net.minecraft.world.effect.MobEffect;
import modernmods.modernfoundry.gadgets.item.PiggyBackPackItem.CarryPotionEffect;
import modernmods.modernfoundry.gadgets.item.ShootProjectileDispenserBehavior;
import modernmods.modernfoundry.gadgets.item.ShurikenItem;
import modernmods.modernfoundry.shared.TinkerFood;
import modernmods.modernfoundry.world.block.FoliageType;

/**
 * Contains any special tools unrelated to the base tools.
 * TODO: consider merging this into commons, the distinction of what is a gadget is getting pretty narrow.
 */
@SuppressWarnings("unused")
public final class TinkerGadgets extends TinkerModule {
  /* Block base properties */

  /*
   * Blocks
   */
  public static final ItemObject<PunjiBlock> punji = BLOCKS.register("punji", () -> new PunjiBlock(builder(MapColor.PLANT, SoundType.GRASS).strength(3.0F).speedFactor(0.4F).noOcclusion().pushReaction(PushReaction.DESTROY)), TOOLTIP_BLOCK_ITEM);

  /*
   * Items
   */
  public static final ItemObject<PiggyBackPackItem> piggyBackpack = ITEMS.register("piggy_backpack", () -> new PiggyBackPackItem(new Properties().stacksTo(16)));
  public static final EnumObject<FrameType,FancyItemFrameItem> itemFrame = ITEMS.registerEnum(FrameType.values(), "item_frame", (type) -> new FancyItemFrameItem(ITEM_PROPS, (world, pos, dir) -> new FancyItemFrameEntity(world, pos, dir, type)));

  // throwballs
  @Deprecated
  public static final ItemObject<GlowBallItem> glowBall;
  @Deprecated
  public static final ItemObject<EFLNItem> efln;
  @Deprecated
  public static final ItemObject<ShurikenItem> quartzShuriken, flintShuriken;
  static {
    Item.Properties THROWABLE_PROPS = new Item.Properties().stacksTo(16);
    glowBall = ITEMS.register("glow_ball", () -> new GlowBallItem(THROWABLE_PROPS));
    efln = ITEMS.register("efln_ball", () -> new EFLNItem(THROWABLE_PROPS));
    quartzShuriken = ITEMS.register("quartz_shuriken", () -> new ShurikenItem(THROWABLE_PROPS, QuartzShurikenEntity::new));
    flintShuriken = ITEMS.register("flint_shuriken", () -> new ShurikenItem(THROWABLE_PROPS, FlintShurikenEntity::new));

  }

  // foods
  public static final EnumObject<FoliageType,FoodCakeBlock> cake;
  public static final ItemObject<FoodCakeBlock> magmaCake;
  static {
    BlockBehaviour.Properties CAKE = builder(SoundType.WOOL).forceSolidOn().strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY);
    cake = BLOCKS.registerEnum(FoliageType.values(), "cake", type -> {
      if (type == FoliageType.ICHOR) {
        return new InvertedCakeBlock(CAKE, TinkerFood.ICHOR_CAKE, EffectCombination.BLOCK);
      }
      return new FoodCakeBlock(CAKE, TinkerFood.getCake(type), type == FoliageType.ENDER ? EffectCombination.ADD : EffectCombination.BLOCK);
    }, UNSTACKABLE_BLOCK_ITEM);
    magmaCake = BLOCKS.register("magma_cake", () -> new FoodCakeBlock(CAKE, TinkerFood.MAGMA_CAKE, EffectCombination.BLOCK), UNSTACKABLE_BLOCK_ITEM);
  }

  // Shurikens

  /*
   * Entities
   */
  public static final DeferredHolder<? super EntityType<FancyItemFrameEntity>, EntityType<FancyItemFrameEntity>> itemFrameEntity = ENTITIES.register("fancy_item_frame", () ->
    EntityType.Builder.<FancyItemFrameEntity>of(
      FancyItemFrameEntity::new, MobCategory.MISC)
      .sized(0.5F, 0.5F)
      .setTrackingRange(10)
      .setUpdateInterval(Integer.MAX_VALUE)
      .setShouldReceiveVelocityUpdates(false)
  );
  @Deprecated
  public static final DeferredHolder<? super EntityType<GlowballEntity>, EntityType<GlowballEntity>> glowBallEntity = ENTITIES.register("glow_ball", () ->
    EntityType.Builder.<GlowballEntity>of(GlowballEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(4)
      .setUpdateInterval(10)
      .setShouldReceiveVelocityUpdates(true)
  );
  @Deprecated
  public static final DeferredHolder<? super EntityType<EFLNEntity>, EntityType<EFLNEntity>> eflnEntity = ENTITIES.register("efln_ball", () ->
    EntityType.Builder.<EFLNEntity>of(EFLNEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(4)
      .setUpdateInterval(10)
      .setShouldReceiveVelocityUpdates(true));
  @Deprecated
  public static final DeferredHolder<? super EntityType<QuartzShurikenEntity>, EntityType<QuartzShurikenEntity>> quartzShurikenEntity = ENTITIES.register("quartz_shuriken", () ->
    EntityType.Builder.<QuartzShurikenEntity>of(QuartzShurikenEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(4)
      .setUpdateInterval(10)
      .setShouldReceiveVelocityUpdates(true)
  );
  @Deprecated
  public static final DeferredHolder<? super EntityType<FlintShurikenEntity>, EntityType<FlintShurikenEntity>> flintShurikenEntity = ENTITIES.register("flint_shuriken", () ->
    EntityType.Builder.<FlintShurikenEntity>of(FlintShurikenEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(4)
      .setUpdateInterval(10)
      .setShouldReceiveVelocityUpdates(true)
  );

  /*
   * Potions
   */
  public static final DeferredHolder<MobEffect, CarryPotionEffect> carryEffect = MOB_EFFECTS.register("carry", CarryPotionEffect::new);

  /*
   * Events
   */
  @SubscribeEvent
  void commonSetup(final FMLCommonSetupEvent event) {
    PiggybackCapability.register();
    event.enqueueWork(() -> {
      cake.forEach(block -> ComposterBlock.COMPOSTABLES.put(block.asItem(), 1.0f));
      ComposterBlock.COMPOSTABLES.put(magmaCake.get().asItem(), 1.0f);

      DispenserBlock.registerBehavior(glowBall, new ShootProjectileDispenserBehavior(glowBallEntity.get()));
      DispenserBlock.registerBehavior(efln, new ShootProjectileDispenserBehavior(eflnEntity.get()));
      DispenserBlock.registerBehavior(flintShuriken, new ShootProjectileDispenserBehavior(flintShurikenEntity.get()));
      DispenserBlock.registerBehavior(quartzShuriken, new ShootProjectileDispenserBehavior(quartzShurikenEntity.get()));
    });
  }

  /** Adds all relevant items to the creative tab, called by general tab */
  public static void addTabItems(ItemDisplayParameters itemDisplayParameters, Output output) {
    output.accept(punji);
    accept(output, itemFrame);
    output.accept(piggyBackpack);
    accept(output, cake);
    output.accept(magmaCake);
  }
}
