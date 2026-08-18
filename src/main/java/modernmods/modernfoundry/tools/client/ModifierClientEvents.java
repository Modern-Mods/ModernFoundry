package modernmods.modernfoundry.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.Icons;
import modernmods.modernfoundry.library.events.ToolEquipmentChangeEvent;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.data.FloatMultiplier;
import modernmods.modernfoundry.library.modifiers.modules.technical.ArmorLevelModule;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataKeys;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.item.ranged.ModifiableBowItem;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Orientation2D;
import modernmods.modernfoundry.library.utils.Orientation2D.Orientation1D;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modules.armor.MinimapModule;
import modernmods.modernfoundry.tools.modules.armor.SleevesModule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/** Modifier event hooks that run client side */
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class ModifierClientEvents {
  @SubscribeEvent
  static void onTooltipEvent(ItemTooltipEvent event) {
    // suppress durability from advanced, we display our own
    if (event.getItemStack().getItem() instanceof IModifiableDisplay) {
      event.getToolTip().removeIf(text -> {

        if (text.getContents() instanceof TranslatableContents translatable) {
          return translatable.getKey().equals("item.durability");
        }
        return false;
      });
    }
  }

  /** Determines whether to render the given hand based on modifiers */
  @SubscribeEvent
  static void renderHand(RenderHandEvent event) {
    Player player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }
    // when firing your melee weapon with ballista, don't render it in the other hand; makes it look like you duplicated your weapon
    InteractionHand hand = event.getHand();
    ItemStack held = player.getItemInHand(hand);
    ItemStack opposite = player.getItemInHand(Util.getOpposite(hand));
    if (!held.isEmpty() && !opposite.isEmpty() && opposite.is(TinkerTags.Items.BALLISTAS) && ModifierUtil.getPersistentInt(opposite, ModifiableBowItem.KEY_BALLISTA, 0) == ModifiableBowItem.FLAG_BALLISTA_HELD) {
      event.setCanceled(true);
      return;
    }

    // if the data is set, render the empty offhand
    if (hand == InteractionHand.OFF_HAND && held.isEmpty()) {
      if (!player.isInvisible() && player.getMainHandItem().getItem() != Items.FILLED_MAP && ArmorLevelModule.getLevel(player, TinkerDataKeys.SHOW_EMPTY_OFFHAND) > 0) {
        PoseStack matrices = event.getPoseStack();
        matrices.pushPose();
        // 26.1.2 RenderHandEvent replaced getMultiBufferSource() with getSubmitNodeCollector() (submit-node pipeline);
        // getPackedLight/getEquipProgress/getSwingProgress are retained. renderPlayerArm now takes the collector.
        renderPlayerArm(matrices, event.getSubmitNodeCollector(), event.getPackedLight(), event.getEquipProgress(), event.getSwingProgress(), player.getMainArm().getOpposite());
        matrices.popPose();
        event.setCanceled(true);
      }
    }
  }

  /** Handles the zoom modifier zooming */
  @SubscribeEvent
  static void handleZoom(ComputeFovModifierEvent event) {
    TinkerDataCapability.getCapability(event.getPlayer()).ifPresent(data -> {
      float newFov = event.getNewFovModifier();

      // scaled effects only apply if we have FOV scaling, nothing to do if 0
      float effectScale = Minecraft.getInstance().options.fovEffectScale().get().floatValue();
      if (effectScale > 0) {
        FloatMultiplier scaledZoom = data.get(TinkerDataKeys.SCALED_FOV_MODIFIER);
        if (scaledZoom != null) {
          // much easier when 1, save some effort
          if (effectScale == 1) {
            newFov *= scaledZoom.getValue();
          } else {
            // unlerp the fov before multiplitying to make sure we apply the proper amount
            // we could use the original FOV, but someone else may have modified it
            float original = event.getFovModifier();
            newFov *= Mth.lerp(effectScale, 1.0F, scaledZoom.getValue() * original) / original;
          }
        }
      }

      // non-scaled effects are much easier to deal with
      FloatMultiplier constZoom = data.get(TinkerDataKeys.FOV_MODIFIER);
      if (constZoom != null) {
        newFov *= constZoom.getValue();
      }
      event.setNewFovModifier(newFov);
    });
  }


  /* Renders the next shield strap item above the offhand item */

  /** Cache of the current item to render */
  private static final int SLOT_BACKGROUND_SIZE = 22;
  /** Size of the border around the map */
  private static final int MAP_PADDING = 7;
  /** Total map size */
  private static final int MAP_SIZE = 2 * MAP_PADDING + 128;

  @Nonnull
  private static ItemStack nextOffhand = ItemStack.EMPTY;
  @Nonnull
  private static ItemStack currentSleeve = ItemStack.EMPTY;

  /** Items to render for the item frame modifier */
  private static final List<ItemStack> itemFrames = new ArrayList<>();

  @SubscribeEvent
  static void playerLoggedOut(LoggingOut event) {
    nextOffhand = ItemStack.EMPTY;
    itemFrames.clear();
  }

  /** Update the slot in the first shield slot */
  @SubscribeEvent
  static void equipmentChange(ToolEquipmentChangeEvent event) {
    if (event.getEntity() != Minecraft.getInstance().player) {
      return;
    }
    EquipmentChangeContext context = event.getContext();
    if (Config.CLIENT.renderShieldSlotItem.get()) {
      if (context.getChangedSlot() == EquipmentSlot.LEGS) {
        IToolStackView tool = context.getToolInSlot(EquipmentSlot.LEGS);
        if (tool != null) {
          ModifierEntry entry = tool.getModifiers().getEntry(TinkerModifiers.shieldStrap.getId());
          if (entry != ModifierEntry.EMPTY) {
            nextOffhand = entry.getHook(ToolInventoryCapability.HOOK).getStack(tool, entry, 0);
            return;
          }
        }
        nextOffhand = ItemStack.EMPTY;
      }
    }
    if (Config.CLIENT.renderSleevesItem.get()) {
      if (context.getChangedSlot() == EquipmentSlot.CHEST) {
        IToolStackView tool = context.getToolInSlot(EquipmentSlot.CHEST);
        if (tool != null) {
          ModifierEntry entry = tool.getModifiers().getEntry(TinkerModifiers.sleeves.getId());
          if (entry != ModifierEntry.EMPTY) {
            currentSleeve = entry.getHook(ToolInventoryCapability.HOOK).getStack(tool, entry, tool.getPersistentData().getInt(SleevesModule.SELECTED_SLOT));
            return;
          }
        }
        currentSleeve = ItemStack.EMPTY;
      }
    }

    if (Config.CLIENT.renderItemFrame.get()) {
      if (context.getChangedSlot() == EquipmentSlot.HEAD) {
        itemFrames.clear();
        IToolStackView tool = context.getToolInSlot(EquipmentSlot.HEAD);
        if (tool != null) {
          ModifierEntry entry = tool.getModifier(TinkerModifiers.itemFrame.getId());
          if (entry.intEffectiveLevel() > 0) {
            entry.getHook(ToolInventoryCapability.HOOK).getAllStacks(tool, entry, itemFrames);
          }
        }
      }
    }
  }

  /** Gets the offset to apply for potion effects on the player */
  private static int getEffectOffset(Player player) {
    boolean hasBeneficial = false;
    for (MobEffectInstance instance : player.getActiveEffects()) {
      if (instance.showIcon() && IClientMobEffectExtensions.of(instance).isVisibleInGui(instance)) {
        if (instance.getEffect().value().isBeneficial()) {
          hasBeneficial = true;
        } else {
          // negative effects means offset two rows
          return 52;
        }
      }
    }
    // if we found a positive effect, only need one row. Otherwise none
    return hasBeneficial ? 26 : 0;
  }

  /** Renders an item slot using the vanilla pop animation */
  private static void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics, int x, int y, DeltaTracker partialTicks, Player player, ItemStack stack, int seed) {
    if (!stack.isEmpty()) {
      float popTime = stack.getPopTime() - partialTicks.getGameTimeDeltaPartialTick(false);
      if (popTime > 0) {
        float scale = 1.0F + popTime / 5.0F;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 8, y + 12);
        graphics.pose().scale(1.0F / scale, (scale + 1.0F) / 2.0F);
        graphics.pose().translate(-(x + 8), -(y + 12));
      }

      graphics.item(player, stack, x, y, seed);
      if (popTime > 0) {
        graphics.pose().popMatrix();
      }
      graphics.itemDecorations(mc.font, stack, x, y);
    }
  }

  /** Renders an empty first person arm for the offhand slot overlay. */
  private static void renderPlayerArm(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, float equipProgress, float swingProgress, HumanoidArm arm) {
    Minecraft mc = Minecraft.getInstance();
    if (!(mc.player instanceof AbstractClientPlayer player)) {
      return;
    }
    boolean rightArm = arm != HumanoidArm.LEFT;
    float side = rightArm ? 1.0F : -1.0F;
    float swingRoot = Mth.sqrt(swingProgress);
    float x = -0.3F * Mth.sin(swingRoot * (float)Math.PI);
    float y = 0.4F * Mth.sin(swingRoot * (float)(Math.PI * 2));
    float z = -0.4F * Mth.sin(swingProgress * (float)Math.PI);
    matrices.translate(side * (x + 0.64000005F), y - 0.6F + equipProgress * -0.6F, z - 0.71999997F);
    matrices.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
    float swingSquared = Mth.sin(swingProgress * swingProgress * (float)Math.PI);
    float swing = Mth.sin(swingRoot * (float)Math.PI);
    matrices.mulPose(Axis.YP.rotationDegrees(side * swing * 70.0F));
    matrices.mulPose(Axis.ZP.rotationDegrees(side * swingSquared * -20.0F));
    matrices.translate(side * -1.0F, 3.6F, 3.5F);
    matrices.mulPose(Axis.ZP.rotationDegrees(side * 120.0F));
    matrices.mulPose(Axis.XP.rotationDegrees(200.0F));
    matrices.mulPose(Axis.YP.rotationDegrees(side * -135.0F));
    matrices.translate(side * 5.6F, 0.0F, 0.0F);
    // Resolve the avatar renderer for the arm (PlayerRenderer was renamed to AvatarRenderer in 26.1).
    AvatarRenderer renderer = (AvatarRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
    // DEFERRED RENDER: renderRightHand/renderLeftHand moved from (PoseStack, MultiBufferSource, int, AbstractClientPlayer)
    // to the new submit-node pipeline (PoseStack, SubmitNodeCollector, int lightCoords, Identifier skinTexture, boolean
    // hasSleeve, AbstractClientPlayer). Drawing the empty-offhand arm needs a SubmitNodeCollector plus the player's skin
    // texture/sleeve flag from the new render path (buffer/light/arm above feed that call); validated in-game once re-hooked.
    if (renderer == null) {
      return;
    }
  }

  /** Render the item in the first shield slot */
  @SubscribeEvent
  public static void renderHotbar(RenderGuiLayerEvent.Post event) {
    Minecraft mc = Minecraft.getInstance();
    Player player = mc.player;
    if (mc.options.hideGui || !VanillaGuiLayers.HOTBAR.equals(event.getName()) || player == null || player != mc.getCameraEntity()) {
      return;
    }
    boolean renderShield = Config.CLIENT.renderShieldSlotItem.get() && !nextOffhand.isEmpty();
    boolean renderSleeves = Config.CLIENT.renderSleevesItem.get() && !currentSleeve.isEmpty();
    boolean renderItemFrame = Config.CLIENT.renderItemFrame.get() && !itemFrames.isEmpty();
    // fetch map stack instance
    float mapScale = Config.CLIENT.mapScale.get().floatValue();
    ItemStack map = ItemStack.EMPTY;
    if (mapScale > 0) {
      TinkerDataCapability.Holder data = TinkerDataCapability.getData(player);
      if (data != null) {
        map = data.get(MinimapModule.MAP, ItemStack.EMPTY);
      }
    }
    if (!renderItemFrame && !renderShield && !renderSleeves && map.isEmpty()) {
      return;
    }
    MultiPlayerGameMode playerController = mc.gameMode;
    if (playerController != null && playerController.getPlayerMode() != GameType.SPECTATOR) {
      // 26.1: RenderSystem.enableBlend/defaultBlendFunc removed; GUI blending is pipeline-managed

      int scaledWidth = mc.getWindow().getGuiScaledWidth();
      int scaledHeight = mc.getWindow().getGuiScaledHeight();
      GuiGraphicsExtractor graphics = event.getGuiGraphics();
      DeltaTracker partialTicks = event.getPartialTick();

      // want just above the normal offhand item
      boolean emptyOffhand = player.getOffhandItem().isEmpty();
      boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
      if (renderShield) {
        int x = scaledWidth / 2 + (rightHanded ? -117 : 101);
        int y = scaledHeight - 38;
        graphics.blit(Icons.ICONS, x - 3, y - 3, emptyOffhand ? 211 : 189, 0, SLOT_BACKGROUND_SIZE, SLOT_BACKGROUND_SIZE, 256, 256);
        renderSlot(mc, graphics, x, y, partialTicks, player, nextOffhand, 11);
      }
      // want to the side above the normal offhand item
      if (renderSleeves) {
        int x = scaledWidth / 2 + (rightHanded ? -136 : 120);
        int y = scaledHeight - 19;
        graphics.blit(Icons.ICONS, x - 3, y - 3, emptyOffhand ? 211 : rightHanded ? 145 : 123, 0, SLOT_BACKGROUND_SIZE, SLOT_BACKGROUND_SIZE, 256, 256);
        renderSlot(mc, graphics, x, y, partialTicks, player, currentSleeve, 11);
      }

      // TODO: cannot remember why this was needed before. Reconfirm if bug still exists.
      // skip the non-hotbar renderers when the pause screen is open, as they can sometimes show up above elements they shouldn't
      // if (mc.screen != null && mc.screen.isPauseScreen()) {
      //   return;
      // }

      // render map
      Orientation2D mapLocation = null;
      int mapOffset = 0;
      if (!map.isEmpty() && mc.level != null) {
        MapItemSavedData data = MapItem.getSavedData(map, mc.level);
        MapId index = map.get(DataComponents.MAP_ID);

        // determine placement of the map
        mapLocation = Config.CLIENT.mapLocation.get();
        Orientation1D xOrientation = mapLocation.getX();
        Orientation1D yOrientation = mapLocation.getY();
        mapOffset = (int) (MAP_SIZE * mapScale);
        int xStart = xOrientation.align(scaledWidth - mapOffset) + Config.CLIENT.mapXOffset.get();
        int yStart = yOrientation.align(scaledHeight - mapOffset) + Config.CLIENT.mapYOffset.get();

        // if top right, compute potion offset
        if (mapLocation == Orientation2D.TOP_RIGHT) {
          int effectOffset = getEffectOffset(player);
          yStart += effectOffset;
          mapOffset += effectOffset;
        }

        // DEFERRED RENDER: the minimap background and map face were drawn with the removed RenderType.text factory and the
        // pre-26.1 MapRenderer#render signature; the RenderType system now builds pipelines via RenderType.create(name,
        // RenderSetup) and map rendering moved to the new render pipeline. The map placement/offset above is kept (it also
        // positions the item frame overlay below), but the resolved data/index (drawn below in the pre-26.1 code) now need
        // the new pipeline to render and are validated in-game.
      }

      if (renderItemFrame) {
        // determine how many items need to be rendered
        int columns = Config.CLIENT.itemsPerRow.get();
        int count = itemFrames.size();
        // need to split items over multiple lines potentially
        int rows = count / columns;
        int inLastRow = count % columns;
        // if we have an exact number, means we should have full in last row
        if (inLastRow == 0) {
          inLastRow = columns;
        } else {
          // we have an incomplete row that was not counted
          rows++;
        }
        // determine placement of the items
        Orientation2D location = Config.CLIENT.itemFrameLocation.get();
        Orientation1D xOrientation = location.getX();
        Orientation1D yOrientation = location.getY();
        int xStart = xOrientation.align(scaledWidth - SLOT_BACKGROUND_SIZE * columns) + Config.CLIENT.itemFrameXOffset.get();
        int yStart = yOrientation.align(scaledHeight - SLOT_BACKGROUND_SIZE * rows) + Config.CLIENT.itemFrameYOffset.get();
        // if the map and item frame are at the same spot, offset item frame below
        if (location == mapLocation) {
          switch (yOrientation) {
            case START -> yStart += mapOffset;
            // add in an extra half set of the slots as we don't want to center it since the map took center
            case MIDDLE -> yStart += (mapOffset + SLOT_BACKGROUND_SIZE * rows) / 2;
            case END -> yStart -= mapOffset;
          }
        }
        // handle potions as well, though its already been handled in the map offset if present
        else if (location == Orientation2D.TOP_RIGHT) {
          yStart += getEffectOffset(player);
        }

        // draw backgrounds
        int lastRow = rows - 1;
        for (int r = 0; r < lastRow; r++) {
          for (int c = 0; c < columns; c++) {
            graphics.blit(Icons.ICONS, xStart + c * SLOT_BACKGROUND_SIZE, yStart + r * SLOT_BACKGROUND_SIZE, 167, 0, SLOT_BACKGROUND_SIZE, SLOT_BACKGROUND_SIZE, 256, 256);
          }
        }
        // last row will be aligned in the direction of x orientation (center, left, or right)
        int lastRowOffset = xOrientation.align((columns - inLastRow) * 2) * SLOT_BACKGROUND_SIZE / 2;
        for (int c = 0; c < inLastRow; c++) {
          graphics.blit(Icons.ICONS, xStart + c * SLOT_BACKGROUND_SIZE + lastRowOffset, yStart + lastRow * SLOT_BACKGROUND_SIZE, 167, 0, SLOT_BACKGROUND_SIZE, SLOT_BACKGROUND_SIZE, 256, 256);
        }

        // draw items
        int i = 0;
        xStart += 3; yStart += 3; // offset from item start instead of frame start
        for (int r = 0; r < lastRow; r++) {
          for (int c = 0; c < columns; c++) {
            renderSlot(mc, graphics, xStart + c * SLOT_BACKGROUND_SIZE, yStart + r * SLOT_BACKGROUND_SIZE, partialTicks, player, itemFrames.get(i), i);
            i++;
          }
        }
        // align last row
        for (int c = 0; c < inLastRow; c++) {
          renderSlot(mc, graphics, xStart + c * SLOT_BACKGROUND_SIZE + lastRowOffset, yStart + lastRow * SLOT_BACKGROUND_SIZE, partialTicks, player, itemFrames.get(i), i);
          i++;
        }
      }
    }
  }
}
