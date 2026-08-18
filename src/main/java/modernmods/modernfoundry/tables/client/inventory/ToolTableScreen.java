package modernmods.modernfoundry.tables.client.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import modernmods.mantle.client.SafeClientAccess;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.GuiUtil;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.item.ITinkerStationDisplay;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.utils.TinkerTooltipFlags;
import modernmods.modernfoundry.tables.client.inventory.module.InfoPanelScreen;
import modernmods.modernfoundry.tables.menu.TabbedContainerMenu;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Shared logic for the tinker station and the modifier worktable */
public abstract class ToolTableScreen<T extends BlockEntity, C extends TabbedContainerMenu<T>> extends BaseTabbedScreen<T,C> {
  private static final Component MODIFIERS_TEXT = TConstruct.makeTranslation("gui", "tinker_station.modifiers");
  private static final Component UPGRADES_TEXT = TConstruct.makeTranslation("gui", "tinker_station.upgrades");
  private static final Component TRAITS_TEXT = TConstruct.makeTranslation("gui", "tinker_station.traits");

  private static final Identifier ICON_TEXTURE = TConstruct.getResource("textures/gui/icons.png");

  /** Side panels, for tools and modifiers */
  protected final InfoPanelScreen<ToolTableScreen<T,C>,C> tinkerInfo;
  protected final InfoPanelScreen<ToolTableScreen<T,C>,C> modifierInfo;

  protected final Player player;
  @Nullable
  protected ArmorStand armorStandPreview;
  protected boolean enableArmorStandPreview = true;
  protected boolean clickedOnArmorStand = false;

  protected int armorStandX = 0;
  protected int armorStandY = 0;
  protected int armorStandScale = 10;
  protected float armorStandAngle = 0;
  protected double armorStandLastMouseX = -1;

  public ToolTableScreen(C c, Inventory playerInventory, Component title) {
    super(c, playerInventory, title);
    this.player = playerInventory.player;

    this.tinkerInfo = new InfoPanelScreen<>(this, c, playerInventory, title);
    this.tinkerInfo.setTextScale(8/9f);
    this.addModule(this.tinkerInfo);

    this.modifierInfo = new InfoPanelScreen<>(this, c, playerInventory, title);
    this.modifierInfo.setTextScale(7/9f);
    this.addModule(this.modifierInfo);
  }

  @Override
  protected void init() {
    super.init();
    if (enableArmorStandPreview) {
      assert this.minecraft != null;
      assert this.minecraft.level != null;
      this.armorStandPreview = new ArmorStand(this.minecraft.level, 0.0D, 0.0D, 0.0D);
      this.armorStandPreview.setNoBasePlate(true);
      this.armorStandPreview.setShowArms(true);
      this.armorStandPreview.yBodyRot = 210.0F;
      this.armorStandPreview.setXRot(25.0F);
      this.armorStandPreview.yHeadRot = this.armorStandPreview.getYRot();
      this.armorStandPreview.yHeadRotO = this.armorStandPreview.getYRot();
    }
  }

  /**
   * Renders the armor stand
   * @param graphics  Graphics instance
   */
  protected void renderArmorStand(GuiGraphicsExtractor graphics) {
    if (this.armorStandPreview != null) {
      Quaternionf pose = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float)Math.PI).rotateY(this.armorStandAngle);
      // 26.1.2 removed InventoryScreen.renderEntityInInventory(center, scale, pose) in favor of the entity-render-state pipeline.
      // Inline the render-state build (mirrors InventoryScreen#renderEntityInInventoryFollowsAngle) so we keep the exact custom
      // tilt pose above (which the FollowsAngle overload would overwrite from mouse-derived angles). Bbox derived from the old
      // center (armorStandX, armorStandY) + scale; positioning/scale is an in-game-validation flag.
      EntityRenderState renderState = Minecraft.getInstance().getEntityRenderDispatcher()
        .getRenderer(this.armorStandPreview).createRenderState(this.armorStandPreview, 1.0F);
      renderState.shadowPieces.clear();
      renderState.outlineColor = 0;
      int s = this.armorStandScale;
      graphics.entity(renderState, this.armorStandScale, new Vector3f(), pose, null,
        this.armorStandX - s, this.armorStandY - 2 * s, this.armorStandX + s, this.armorStandY + s);

      graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, armorStandX - 16, armorStandY - 16, 0f, 184f, 32, 32, 256, 256);
    }
  }

 /**
  * Setup clickable areas and position for the armor stand
  * @param x         Stand X position
  * @param y         Stand Y position
  * @param scale     Stand size
  */
  protected void setupArmorStandPreview(int x, int y, int scale) {
    this.armorStandX = this.cornerX + x;
    this.armorStandY = this.cornerY + y;
    this.armorStandScale = scale;
  }

  /** Updates the item displayed on the armor stand */
  protected void updateArmorStandPreview(ItemStack stack) {
    if (this.armorStandPreview != null) {
      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
        this.armorStandPreview.setItemSlot(equipmentslot, ItemStack.EMPTY);
      }

      if (!stack.isEmpty()) {
        ItemStack copy = stack.copy();
        Item item = stack.getItem();
        // 26.1: ArmorItem removed; resolve the armor slot from the equippable data component
        Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
          this.armorStandPreview.setItemSlot(equippable.slot(), copy);
        } else {
          this.armorStandPreview.setItemSlot(EquipmentSlot.OFFHAND, copy);
        }
      }

    }
  }

  /** Updates the tool panel area */
  protected void updateToolPanel(LazyToolStack lazyToolStack) {
    ToolStack tool = lazyToolStack.getTool();
    if (tool.getItem() instanceof ITinkerStationDisplay display) {
      tinkerInfo.setCaption(display.getLocalizedName());
      tinkerInfo.setText(display.getStatInformation(tool, Minecraft.getInstance().player, new ArrayList<>(), SafeClientAccess.getTooltipKey(), TinkerTooltipFlags.TINKER_STATION));
    }
    else {
      ItemStack result = lazyToolStack.getStack();
      tinkerInfo.setCaption(result.getHoverName());
      List<Component> list = new ArrayList<>();
      result.getItem().appendHoverText(result, TooltipContext.of(Minecraft.getInstance().level), net.minecraft.world.item.component.TooltipDisplay.DEFAULT, list::add, Default.NORMAL);
      tinkerInfo.setText(list);
    }
  }

  /** Updates the modifier panel with relevant info */
  protected void updateModifierPanel(ToolStack tool) {
    RegistryAccess access = player.level().registryAccess();
    List<Component> modifierNames = new ArrayList<>();
    List<Component> modifierTooltip = new ArrayList<>();
    Component title;
    // control displays just traits, bit trickier to do
    if (modernmods.modernfoundry.library.client.ScreenUtil.hasControlDown()) {
      title = TRAITS_TEXT;
      Map<Modifier,Integer> upgrades = tool.getUpgrades().getModifiers().stream()
                                           .collect(Collectors.toMap(ModifierEntry::getModifier, ModifierEntry::getLevel, Integer::sum));
      for (ModifierEntry entry : tool.getModifierList()) {
        Modifier mod = entry.getModifier();
        if (mod.shouldDisplay(true)) {
          int level = entry.getLevel() - upgrades.getOrDefault(mod, 0);
          if (level > 0) {
            ModifierEntry trait = new ModifierEntry(entry.getModifier(), level);
            modifierNames.add(mod.getDisplayName(tool, trait, access));
            modifierTooltip.add(mod.getDescription(tool, trait));
          }
        }
      }
    } else {
      // shift is just upgrades/abilities, otherwise all
      List<ModifierEntry> modifiers;
      if (modernmods.modernfoundry.library.client.ScreenUtil.hasShiftDown()) {
        modifiers = tool.getUpgrades().getModifiers();
        title = UPGRADES_TEXT;
      } else {
        modifiers = tool.getModifierList();
        title = MODIFIERS_TEXT;
      }
      for (ModifierEntry entry : modifiers) {
        Modifier mod = entry.getModifier();
        if (mod.shouldDisplay(true)) {
          modifierNames.add(mod.getDisplayName(tool, entry, access));
          modifierTooltip.add(mod.getDescription(tool, entry));
        }
      }
    }

    modifierInfo.setCaption(title);
    modifierInfo.setText(modifierNames, modifierTooltip);
  }

  @Override
  public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
    double mouseX = event.x(); double mouseY = event.y(); int mouseButton = event.button();
    int armorStandBoxW = this.armorStandScale + 30;
    int armorStandBoxH = this.armorStandScale * 2;
    int armorStandBoxX = this.armorStandX - armorStandBoxW / 2;
    int armorStandBoxY = this.armorStandY - armorStandBoxH + 5;
    this.clickedOnArmorStand = this.enableArmorStandPreview && GuiUtil.isHovered((int) mouseX, (int) mouseY, armorStandBoxX, armorStandBoxY, armorStandBoxW, armorStandBoxH);

    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
    double mouseX = event.x(); double mouseY = event.y(); int state = event.button();
    this.clickedOnArmorStand = false;
    this.armorStandLastMouseX = -1;

    return super.mouseReleased(event);
  }

  @Override
  public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double timeSinceLastClick, double unkowwn) {
    double mouseX = event.x(); double mouseY = event.y(); int clickedMouseButton = event.button();
    if (this.enableArmorStandPreview && this.clickedOnArmorStand && this.armorStandLastMouseX != -1) {
      this.armorStandAngle += (float) (mouseX - this.armorStandLastMouseX) / 10f;
    }
    this.armorStandLastMouseX = mouseX;

    return super.mouseDragged(event, timeSinceLastClick, unkowwn);
  }
}
