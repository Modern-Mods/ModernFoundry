package modernmods.modernfoundry.tools;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import modernmods.mantle.client.ResourceColorManager;
import modernmods.mantle.client.SafeClientAccess;
import modernmods.mantle.client.TooltipKey;
import modernmods.mantle.data.listener.ISafeManagerReloadListener;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.ClientEventBase;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.client.armor.AbstractArmorModel;
import modernmods.modernfoundry.library.client.armor.ArmorModelManager;
import modernmods.modernfoundry.library.client.armor.texture.TrimArmorTextureSupplier;
import modernmods.modernfoundry.library.client.book.content.AbstractMaterialContent;
import modernmods.modernfoundry.library.client.materials.MaterialTooltipCache;
import modernmods.modernfoundry.library.client.model.DynamicTextureLoader;
import modernmods.modernfoundry.library.client.model.TinkerItemProperties;
import modernmods.modernfoundry.library.client.model.tools.MaterialBlockModel;
import modernmods.modernfoundry.library.client.model.tools.MaterialModel;
import modernmods.modernfoundry.library.client.model.tools.ToolModel;
import modernmods.modernfoundry.library.client.modifiers.DyedModifierModel;
import modernmods.modernfoundry.library.client.modifiers.FluidModifierModel;
import modernmods.modernfoundry.library.client.modifiers.MaterialModifierModel;
import modernmods.modernfoundry.library.client.modifiers.ModifierModelManager;
import modernmods.modernfoundry.library.client.modifiers.ModifierModelManager.ModifierModelRegistrationEvent;
import modernmods.modernfoundry.library.client.modifiers.ModifierModelMapManager;
import modernmods.modernfoundry.library.client.modifiers.NormalModifierModel;
import modernmods.modernfoundry.library.client.modifiers.PotionModifierModel;
import modernmods.modernfoundry.library.client.modifiers.TankModifierModel;
import modernmods.modernfoundry.library.client.modifiers.TrimModifierModel;
import modernmods.modernfoundry.library.client.particle.AttackParticle;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.ModifierManager;
import modernmods.modernfoundry.library.modifiers.modules.technical.ArmorStatModule;
import modernmods.modernfoundry.library.tools.capability.TinkerDataKeys;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.library.utils.HarvestTiers;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.shared.TinkerAttributes;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.client.CrystalshotRenderer;
import modernmods.modernfoundry.tools.client.FluidEffectProjectileRenderer;
import modernmods.modernfoundry.tools.client.OverslimeModifierModel;
import modernmods.modernfoundry.tools.client.ShieldBannerModifierSpriteSource;
import modernmods.modernfoundry.tools.client.SlimeskullArmorModel;
import modernmods.modernfoundry.tools.client.ToolContainerScreen;
import modernmods.modernfoundry.tools.client.material.CombatFishingHookRenderer;
import modernmods.modernfoundry.tools.client.material.ThrownShurikenRenderer;
import modernmods.modernfoundry.tools.client.material.ThrownToolRenderer;
import modernmods.modernfoundry.tools.item.ModifierCrystalItem;
import modernmods.modernfoundry.tools.logic.DoubleJumpHandler;
import modernmods.modernfoundry.tools.logic.InteractionHandler;
import modernmods.modernfoundry.tools.modules.ranged.ammo.SmashingModule;
import modernmods.modernfoundry.tools.network.TinkerControlPacket;

import java.util.function.Consumer;

import static modernmods.modernfoundry.TConstruct.getResource;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class ToolClientEvents extends ClientEventBase {
  /** Keybinding category for all Tinkers' Construct key mappings; label key resolves to key.category.modernfoundry.modernfoundry */
  private static final KeyMapping.Category TCONSTRUCT_CATEGORY = new KeyMapping.Category(TConstruct.getResource("modernfoundry"));
  /** Keybinding for interacting using a helmet */
  private static final KeyMapping HELMET_INTERACT = new KeyMapping(TConstruct.makeTranslationKey("key", "helmet_interact"), KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.z"), TCONSTRUCT_CATEGORY);
  /** Keybinding for interacting using leggings */
  private static final KeyMapping LEGGINGS_INTERACT = new KeyMapping(TConstruct.makeTranslationKey("key", "leggings_interact"), KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.i"), TCONSTRUCT_CATEGORY);

  /** Listener to clear modifier cache */
  private static final ISafeManagerReloadListener MODIFIER_RELOAD_LISTENER = manager -> {
    ModifierManager.INSTANCE.getAllValues().forEach(modifier -> modifier.clearCache(PackType.CLIENT_RESOURCES));
  };

  @SubscribeEvent
  static void addResourceListener(AddClientReloadListenersEvent manager) {
    ModifierModelManager.init(manager);
    manager.addListener(TConstruct.getResource("modifier_model_map"), ModifierModelMapManager.INSTANCE);
    MaterialTooltipCache.init(manager);
    DynamicTextureLoader.init(manager);
    manager.addListener(TConstruct.getResource("tool_modifier_reload"), MODIFIER_RELOAD_LISTENER);
    manager.addListener(TConstruct.getResource("slimeskull_armor_model"), SlimeskullArmorModel.RELOAD_LISTENER);
    manager.addListener(TConstruct.getResource("harvest_tiers"), HarvestTiers.RELOAD_LISTENER);
    ArmorModelManager.init(manager);
    manager.addListener(TConstruct.getResource("trim_armor_texture"), TrimArmorTextureSupplier.CACHE_INVALIDATOR);
  }

  @SubscribeEvent
  static void registerSpriteSourceTypes(RegisterSpriteSourcesEvent event) {
    ShieldBannerModifierSpriteSource.register(event);
  }

  @SubscribeEvent
  static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
    modernmods.modernfoundry.library.client.TinkerRenderTypes.registerPipelines(event::registerPipeline);
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
    // block model loaders still use the unbaked model loader registry; item models (material, tool) moved to RegisterItemModelsEvent
    event.register(getResource("material_block"), MaterialBlockModel.LOADER);
  }

  @SubscribeEvent
  static void registerItemModels(RegisterItemModelsEvent event) {
    event.register(MaterialModel.ID, MaterialModel.Unbaked.MAP_CODEC);
    event.register(ToolModel.ID, ToolModel.Unbaked.MAP_CODEC);
  }

  @SubscribeEvent
  static void registerItemTintSources(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources event) {
    // restores the per-modifier color of modifier crystals, lost with the removed ItemColors system
    event.register(modernmods.modernfoundry.tools.client.ModifierCrystalTintSource.ID, modernmods.modernfoundry.tools.client.ModifierCrystalTintSource.MAP_CODEC);
  }

  /** Registers the item client extensions, replacing the removed Item#initializeClient in 26.1 */
  @SubscribeEvent
  static void registerClientItemExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
    for (Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
      if (item instanceof modernmods.modernfoundry.library.tools.item.armor.MultilayerArmorItem armor) {
        armor.initializeClient(ext -> event.registerItem(ext, item));
      } else if (item instanceof modernmods.modernfoundry.tools.item.SlimeskullItem skull) {
        skull.initializeClient(ext -> event.registerItem(ext, item));
      } else if (item instanceof modernmods.modernfoundry.library.tools.item.ranged.ModifiableLauncherItem launcher) {
        launcher.initializeClient(ext -> event.registerItem(ext, item));
      } else if (item instanceof modernmods.modernfoundry.library.tools.item.ModifiableItem modifiable) {
        modifiable.initializeClient(ext -> event.registerItem(ext, item));
      }
    }
    // mob effect extensions (CarryPotionEffect is a TinkerEffect whose visibility is registered by TinkerEffect.ClientExtensions;
    // its custom carry icons are deferred to a future re-hook)
    for (net.minecraft.world.effect.MobEffect effect : net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT) {
      if (effect instanceof modernmods.modernfoundry.tools.modifiers.effect.HelmetChargingEffect charging) {
        charging.initializeClient(ext -> event.registerMobEffect(ext, effect));
      }
    }
    // 26.1.2 removed FluidType#initializeClient; the potion fluid's per-stack tint now lives in PotionFluidTintSource
    // wired via FluidClientEvents#registerFluidModels, so no client fluid-type extension needs registering here.
  }

  @SubscribeEvent
  static void registerModifierModels(ModifierModelRegistrationEvent event) {
    event.registerModel(getResource("normal"), NormalModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("overslime"), OverslimeModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("fluid"), FluidModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("tank"), TankModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("material"), MaterialModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("dyed"), DyedModifierModel.UNBAKED_INSTANCE);
    // trim shows up as valid on every tool, skip to reduce memory overhead on tools using the new system - add it using the new system if you want it
    event.registerModel(getResource("trim"), TrimModifierModel.UNBAKED_INSTANCE);
    ModifierModelMapManager.legacyBlacklist(TrimModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("potion"), PotionModifierModel.UNBAKED_INSTANCE);
    event.registerModel(getResource("smashing_fluid"), new FluidModifierModel.Unbaked(SmashingModule.TANK_HELPER));
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(TinkerTools.indestructibleItem.get(), ItemEntityRenderer::new);
    event.registerEntityRenderer(TinkerTools.crystalshotEntity.get(), CrystalshotRenderer::new);
    event.registerEntityRenderer(TinkerTools.fishingHook.get(), CombatFishingHookRenderer::new);
    // TODO: config option for vanilla style renderer?
    event.registerEntityRenderer(TinkerTools.materialArrow.get(), ThrownToolRenderer::new);
    event.registerEntityRenderer(TinkerTools.thrownShuriken.get(), ThrownShurikenRenderer::new);
    event.registerEntityRenderer(TinkerTools.thrownTool.get(), ThrownToolRenderer::new);
    event.registerEntityRenderer(TinkerModifiers.fluidSpitEntity.get(), FluidEffectProjectileRenderer::new);
    event.registerEntityRenderer(TinkerModifiers.fireball.get(), context -> new ThrownItemRenderer<>(context, 0.75f, true));
  }

  @SubscribeEvent
  static void registerKeyBinding(RegisterKeyMappingsEvent event) {
    event.registerCategory(TCONSTRUCT_CATEGORY);
    event.register(HELMET_INTERACT);
    event.register(LEGGINGS_INTERACT);
  }

  @SubscribeEvent
  static void clientSetupEvent(FMLClientSetupEvent event) {
    NeoForge.EVENT_BUS.addListener(ToolClientEvents::handleKeyBindings);
    NeoForge.EVENT_BUS.addListener(ToolClientEvents::handleInput);
    AbstractArmorModel.init();

    // keybinds
    event.enqueueWork(() -> {
      // fake ingot showing in the book is a little nicer than the repair kits
      AbstractMaterialContent.registerFallbackPart(TinkerToolParts.fakeIngot);
      AbstractMaterialContent.registerFallbackPart(TinkerToolParts.fakeStorageBlockItem);
      // properties
      // stone
      TinkerItemProperties.registerToolProperties(TinkerTools.pickaxe);
      TinkerItemProperties.registerToolProperties(TinkerTools.sledgeHammer);
      TinkerItemProperties.registerToolProperties(TinkerTools.veinHammer);
      // dirt
      TinkerItemProperties.registerToolProperties(TinkerTools.mattock);
      TinkerItemProperties.registerToolProperties(TinkerTools.pickadze);
      TinkerItemProperties.registerToolProperties(TinkerTools.excavator);
      // axe
      TinkerItemProperties.registerToolProperties(TinkerTools.handAxe);
      TinkerItemProperties.registerToolProperties(TinkerTools.broadAxe);
      // leaves
      TinkerItemProperties.registerToolProperties(TinkerTools.kama);
      TinkerItemProperties.registerToolProperties(TinkerTools.scythe);
      // sword
      TinkerItemProperties.registerToolProperties(TinkerTools.dagger);
      TinkerItemProperties.registerToolProperties(TinkerTools.sword);
      TinkerItemProperties.registerToolProperties(TinkerTools.cleaver);
      // bow
      TinkerItemProperties.registerCrossbowProperties(TinkerTools.crossbow);
      TinkerItemProperties.registerToolProperties(TinkerTools.longbow);
      TinkerItemProperties.registerToolProperties(TinkerTools.fishingRod);
      TinkerItemProperties.registerToolProperties(TinkerTools.javelin);
      // misc
      TinkerItemProperties.registerToolProperties(TinkerTools.flintAndBrick);
      TinkerItemProperties.registerToolProperties(TinkerTools.skyStaff);
      TinkerItemProperties.registerToolProperties(TinkerTools.earthStaff);
      TinkerItemProperties.registerToolProperties(TinkerTools.ichorStaff);
      TinkerItemProperties.registerToolProperties(TinkerTools.enderStaff);
      // ancient
      TinkerItemProperties.registerToolProperties(TinkerTools.meltingPan);
      TinkerItemProperties.registerCrossbowProperties(TinkerTools.warPick);
      TinkerItemProperties.registerToolProperties(TinkerTools.battlesign);
      TinkerItemProperties.registerToolProperties(TinkerTools.swasher);
      if (ModList.get().isLoaded("twilightforest")) {
        TinkerItemProperties.registerToolProperties(TinkerTools.minotaurAxe);
      }
      // armor
      TinkerItemProperties.registerToolProperties(TinkerTools.travelersShield);
      TinkerItemProperties.registerToolProperties(TinkerTools.plateShield);
      Consumer<Item> brokenConsumer = TinkerItemProperties::registerBrokenProperty;
      TinkerTools.travelersGear.forEach(brokenConsumer);
      TinkerTools.plateArmor.forEach(brokenConsumer);
      TinkerTools.slimesuit.forEach(brokenConsumer);
      TinkerItemProperties.registerToolProperties(TinkerTools.slimeWings);
    });
  }

  @SubscribeEvent
  static void registerMenuScreens(RegisterMenuScreensEvent event) {
    event.register(TinkerTools.toolContainer.get(), ToolContainerScreen::new);
  }

  @SubscribeEvent
  static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    net.minecraft.client.particle.ParticleResources.SpriteParticleRegistration<SimpleParticleType> factory = AttackParticle.Factory::new;
    event.registerSpriteSet(TinkerTools.hammerAttackParticle.get(), factory);
    event.registerSpriteSet(TinkerTools.axeAttackParticle.get(), factory);
    event.registerSpriteSet(TinkerTools.bonkAttackParticle.get(), factory);
  }

  // The pre-26.1 runtime item-color handlers were removed: RegisterColorHandlersEvent.Item and the ItemColors/ItemColor
  // system no longer exist. Tool material and modifier tints are now baked directly into the model quads (see ToolModel /
  // MaterialModel). Remaining dynamic tints such as the modifier crystal color need an ItemTintSource declared in the item
  // model JSON (RegisterColorHandlersEvent.ItemTintSources) and are validated in-game.

  // values to check if a key was being pressed last tick, safe as a static value as we only care about a single player client side
  /** If true, we were jumping last tick */
  private static boolean wasJumping = false;
  /** If true, we were interacting with helmet last tick */
  private static boolean wasHelmetInteracting = false;
  /** If true, we were interacting with leggings last tick */
  private static boolean wasLeggingsInteracting = false;

  /** Called on player tick to handle keybinding presses */
  private static void handleKeyBindings(PlayerTickEvent.Pre event) {
    Minecraft minecraft = Minecraft.getInstance();
    Player player = event.getEntity();
    if (minecraft.player != null && minecraft.player == player && player.level().isClientSide() && !minecraft.player.isSpectator()) {

      // jumping in mid air for double jump
      // ensure we pressed the key since the last tick, holding should not use all your jumps at once
      boolean isJumping = minecraft.options.keyJump.isDown();
      if (!wasJumping && isJumping) {
        if (TinkerEffects.antigravity.get().antigravityJump(player)) {
          TinkerNetwork.getInstance().sendToServer(TinkerControlPacket.ANTIGRAVITY_JUMP);
        }
        else if (DoubleJumpHandler.extraJump(player)) {
          TinkerNetwork.getInstance().sendToServer(TinkerControlPacket.DOUBLE_JUMP);
        }
      }
      wasJumping = isJumping;

      // helmet interaction
      boolean isHelmetInteracting = HELMET_INTERACT.isDown();
      if (!wasHelmetInteracting && isHelmetInteracting) {
        TooltipKey key = SafeClientAccess.getTooltipKey();
        if (InteractionHandler.startArmorInteract(player, EquipmentSlot.HEAD, key)) {
          TinkerNetwork.getInstance().sendToServer(TinkerControlPacket.getStartHelmetInteract(key));
        }
      }
      if (wasHelmetInteracting && !isHelmetInteracting) {
        if (InteractionHandler.stopArmorInteract(player, EquipmentSlot.HEAD)) {
          TinkerNetwork.getInstance().sendToServer(TinkerControlPacket.STOP_HELMET_INTERACT);
        }
      }

      // leggings interaction
      boolean isLeggingsInteract = LEGGINGS_INTERACT.isDown();
      if (!wasLeggingsInteracting && isLeggingsInteract) {
        TooltipKey key = SafeClientAccess.getTooltipKey();
        if (InteractionHandler.startArmorInteract(player, EquipmentSlot.LEGS, key)) {
          TinkerNetwork.getInstance().sendToServer(TinkerControlPacket.getStartLeggingsInteract(key));
        }
      }
      if (wasLeggingsInteracting && !isLeggingsInteract) {
        if (InteractionHandler.stopArmorInteract(player, EquipmentSlot.LEGS)) {
          TinkerNetwork.getInstance().sendToServer(TinkerControlPacket.STOP_LEGGINGS_INTERACT);
        }
      }

      wasHelmetInteracting = isHelmetInteracting;
      wasLeggingsInteracting = isLeggingsInteract;
    }
  }

  @SuppressWarnings("removal")
  private static void handleInput(MovementInputUpdateEvent event) {
    Player player = event.getEntity();
    if (player.isUsingItem() && !player.isPassenger()) {
      ItemStack using = player.getUseItem();
      // start with the attribute
      double speed = player.getAttributeValue(TinkerAttributes.USE_ITEM_SPEED);
      // start by calculating tool stat, not an attribute to ensure both hands get their say
      if (using.is(TinkerTags.Items.HELD)) {
        ToolStack tool = ToolStack.from(using);
        speed += tool.getStats().get(ToolStats.USE_ITEM_SPEED) - ToolStats.USE_ITEM_SPEED.getDefaultValue();
      }
      // next, add in deprecated key bonus
      speed = Mth.clamp(speed + ArmorStatModule.getStat(player, TinkerDataKeys.USE_ITEM_SPEED), 0, 1);
      // The pre-26.1 use-item movement slowdown scaled the mutable Input#leftImpulse/forwardImpulse fields. In 26.1
      // ClientInput exposes only an immutable keyPresses (boolean Input record) consumed via getMoveVector(), so the
      // impulse can no longer be scaled here; re-applying the slowdown needs a movement-speed modifier hook and is
      // validated in-game. The intended slowdown factor is computed below.
      @SuppressWarnings("unused")
      float slowdown = (float) (speed * 5);
    }
  }
}
