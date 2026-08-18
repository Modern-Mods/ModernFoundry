package modernmods.modernfoundry.tools.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import modernmods.mantle.client.screen.ElementScreen;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.fluid.SimpleFluidTank;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.recipe.partbuilder.Pattern;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability.InventoryModifierHook;
import modernmods.modernfoundry.library.tools.layout.Patterns;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.smeltery.client.screen.IScreenWithFluidTank;
import modernmods.modernfoundry.smeltery.client.screen.module.GuiTankModule;
import modernmods.modernfoundry.tools.menu.ToolContainerMenu;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static modernmods.modernfoundry.tools.menu.ToolContainerMenu.REPEAT_BACKGROUND_START;
import static modernmods.modernfoundry.tools.menu.ToolContainerMenu.SLOT_SIZE;
import static modernmods.modernfoundry.tools.menu.ToolContainerMenu.TITLE_SIZE;
import static modernmods.modernfoundry.tools.menu.ToolContainerMenu.UI_START;

/** Screen for a tool inventory */
public class ToolContainerScreen extends AbstractContainerScreen<ToolContainerMenu> implements IScreenWithFluidTank {
  /** The Identifier containing the chest GUI texture. */
  private static final Identifier TEXTURE = TConstruct.getResource("textures/gui/tool_inventory.png");

  /** Slot background for 3x3 crafting grid */
  private static final ElementScreen CRAFTING_SLOTS = new ElementScreen(TEXTURE, 176, 74, 54, 54, 256, 256);
  /** Result slot for 3x3 crafting grid */
  private static final ElementScreen CRAFTING_RESULT = CRAFTING_SLOTS.move(176, 20, 62, 54);
  /** Full 2x2 crafting grid */
  private static final ElementScreen INVENTORY_CRAFTING = CRAFTING_SLOTS.move(176, 128, 74, 36);
  /** Fluid bar to draw at the bottom of the UI */
  private static final ElementScreen FLUID_TANK = CRAFTING_SLOTS.move(0, 224, 176, 14);

  /** Max number of rows in the repeat slots background */
  private static final int REPEAT_BACKGROUND_SIZE = 6 * SLOT_SIZE;
  /** Start location of the player inventory */
  private static final int PLAYER_INVENTORY_START = REPEAT_BACKGROUND_START + REPEAT_BACKGROUND_SIZE;
  /** Height of the player inventory texture */
  private static final int PLAYER_INVENTORY_HEIGHT = 96;
  /** Start Y location of the slot start element */
  private static final int SLOTS_START = 256 - SLOT_SIZE;
  /** Selected slot texture X position */
  private static final int SELECTED_X = 176;

  /** Total number of slots in the inventory */
  private final int slots;
  /** Number of rows in this inventory */
  private final int inventoryRows;
  /** Number of slots in the final row */
  private final int slotsInLastRow;
  /** Tool tank rendering logic */
  @Nullable
  private final GuiTankModule tank;
  /** 26.1.2: AbstractContainerScreen#imageHeight is now final, so compute it before super instead of assigning after. */
  private static int computeImageHeight(ToolContainerMenu menu) {
    int slots = menu.getItemHandler().getSlots();
    if (menu.isShowOffhand()) {
      slots++;
    }
    int inventoryRows = slots / 9;
    if (slots % 9 != 0) {
      inventoryRows++;
    }
    int craftingHeight = menu.getCraftingHeight() * SLOT_SIZE;
    int height = UI_START + TITLE_SIZE + PLAYER_INVENTORY_HEIGHT + inventoryRows * SLOT_SIZE + craftingHeight;
    if (menu.getTank().getCapacity() > 0) {
      height += FLUID_TANK.h;
    }
    return height;
  }

  public ToolContainerScreen(ToolContainerMenu menu, Inventory inv, Component title) {
    super(menu, inv, title, 176, computeImageHeight(menu));
    int slots = menu.getItemHandler().getSlots();
    if (menu.isShowOffhand()) {
      slots++;
    }
    int inventoryRows = slots / 9;
    int slotsInLastRow = slots % 9;
    if (slotsInLastRow == 0) {
      slotsInLastRow = 9;
    } else {
      inventoryRows++;
    }
    this.slots = slots;
    this.inventoryRows = inventoryRows;
    this.slotsInLastRow = slotsInLastRow;
    int craftingHeight = menu.getCraftingHeight() * SLOT_SIZE;
    SimpleFluidTank tank = menu.getTank();
    if (tank.getCapacity() > 0) {
      this.tank = new GuiTankModule(this, tank, 8, this.imageHeight - PLAYER_INVENTORY_HEIGHT - 9, 160, 8, true, null);
    } else {
      this.tank = null;
    }
    if (slots > 0) {
      this.titleLabelY += craftingHeight;
    }
    this.inventoryLabelY = this.imageHeight - 93;
  }

  @Override
  protected void slotClicked(Slot slot, int slotId, int index, ContainerInput type) {
    // disallow swapping the tool slot
    if (type == ContainerInput.SWAP && slot.container == menu.getPlayer().getInventory() && slot.getSlotIndex() == menu.getSlotIndex()) {
      return;
    }
    super.slotClicked(slot, slotId, index, type);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
    int xStart = (this.width - this.imageWidth) / 2;
    int yStart = (this.height - this.imageHeight) / 2;

    // draw the background, repeated if we have too much content for the default size, or shrunk otherwise
    int craftingHeight = menu.getCraftingHeight();
    int slotBackground = REPEAT_BACKGROUND_START + (inventoryRows + craftingHeight) * SLOT_SIZE;
    if (slotBackground < PLAYER_INVENTORY_START) {
      // small background? draw a single segment up to the size
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xStart, yStart, (float)(0), (float)(0), this.imageWidth, slotBackground, 256, 256);
    } else {
      // large background? repeat as needed
      // start with the top bar + roughly 6 slots
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xStart, yStart, (float)(0), (float)(0), this.imageWidth, PLAYER_INVENTORY_START, 256, 256);
      int yOffset = PLAYER_INVENTORY_START;
      int remainingBackground = slotBackground - yOffset;
      // add chunks of about 6 until we run out
      for (; remainingBackground > REPEAT_BACKGROUND_SIZE; remainingBackground -= REPEAT_BACKGROUND_SIZE) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xStart, yStart + yOffset, (float)(0), (float)(REPEAT_BACKGROUND_START), this.imageWidth, REPEAT_BACKGROUND_SIZE, 256, 256);
        yOffset += REPEAT_BACKGROUND_SIZE;
      }
      // draw last partial chunk
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xStart, yStart + yOffset, (float)(0), (float)(REPEAT_BACKGROUND_START), this.imageWidth, remainingBackground, 256, 256);
    }
    // draw tank if we have capacity
    if (tank != null) {
      FLUID_TANK.draw(graphics, xStart, yStart + slotBackground);
      slotBackground += FLUID_TANK.h;
    }
    // draw the player inventory background
    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xStart, yStart + slotBackground, (float)(0), (float)(PLAYER_INVENTORY_START), this.imageWidth, PLAYER_INVENTORY_HEIGHT, 256, 256);

    // add crafting table slots
    // if we have no slots, push them below the title, otherwise above the title
    int craftingOffset = yStart + (slots == 0 ? REPEAT_BACKGROUND_START : UI_START);
    if (craftingHeight == 3) {
      CRAFTING_SLOTS.draw(graphics, xStart + 29, craftingOffset);
      CRAFTING_RESULT.draw(graphics, xStart + 83, craftingOffset);
    } else if (craftingHeight == 2) {
      INVENTORY_CRAFTING.draw(graphics, xStart + 51, craftingOffset);
    }

    // draw slot background
    if (slots > 0) {
      int rowLeft = xStart + 7;
      int rowStart = yStart + REPEAT_BACKGROUND_START - SLOT_SIZE + (craftingHeight * SLOT_SIZE);
      for (int i = 1; i < inventoryRows; i++) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rowLeft, rowStart + i * SLOT_SIZE, (float)(0), (float)(SLOTS_START), 9 * SLOT_SIZE, SLOT_SIZE, 256, 256);
      }
      // last row may not have all slots
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rowLeft, rowStart + inventoryRows * SLOT_SIZE, (float)(0), (float)(SLOTS_START), slotsInLastRow * SLOT_SIZE, SLOT_SIZE, 256, 256);
    }

    // draw a background on the selected slot index
    int slotIndex = menu.getSlotIndex();
    int playerStart = menu.getPlayerInventoryStart();
    int highlightIndex = -1;
    if (slotIndex < 9) {
      // hotbar slots are after all our slots, and after the main inventory 27
      highlightIndex = playerStart + slotIndex + 27;
    } else if (slotIndex < Inventory.INVENTORY_SIZE) {
      // main inventory 27 is after our slots, but the index is 9 too high (hotbar)
      highlightIndex = playerStart + slotIndex - 9;
    } else if (slotIndex == Inventory.SLOT_OFFHAND && menu.isShowOffhand()) {
      // offhand is the last slot, but only if the offhand is shown in the inveotry
      highlightIndex = playerStart - 1;
    }
    // armor is not shown, so that will be -1
    if (highlightIndex != -1 && highlightIndex < menu.slots.size()) {
      Slot slot = menu.getSlot(highlightIndex);
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xStart + slot.x - 2, yStart + slot.y - 2, (float)(SELECTED_X), (float)(0), SLOT_SIZE + 2, SLOT_SIZE + 2, 256, 256);
    }

    // prepare pattern drawing
    assert this.minecraft != null;

    // draw slot patterns for all empty slots
    int start = menu.getToolInventoryStart();
    int maxSlots = menu.slots.size();

    IToolStackView tool = menu.getTool();
    List<ModifierEntry> modifiers = tool.getModifierList();
    modifiers:
    for (int modIndex = modifiers.size() - 1; modIndex >= 0; modIndex--) {
      ModifierEntry entry = modifiers.get(modIndex);
      InventoryModifierHook inventory = entry.getHook(ToolInventoryCapability.HOOK);
      int size = inventory.getSlots(tool, entry);
      for (int i = 0; i < size; i++) {
        if (start + i >= maxSlots) {
          break modifiers;
        }
        Slot slot = menu.getSlot(start + i);
        Pattern pattern = inventory.getPattern(tool, entry, i, slot.hasItem());
        if (pattern != null) {
          graphics.blitSprite(RenderPipelines.GUI_TEXTURED, modernmods.mantle.client.render.FluidRenderer.getBlockSprite(pattern.getTexture()), xStart + slot.x, yStart + slot.y, 16, 16);
        }
      }
      start += size;
    }

    // offhand icon
    if (menu.isShowOffhand()) {
      Slot slot = menu.getSlot(menu.getPlayerInventoryStart() - 1);
      if (!slot.hasItem()) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, modernmods.mantle.client.render.FluidRenderer.getBlockSprite(Patterns.SHIELD.getTexture()), xStart + slot.x, yStart + slot.y, 16, 16);
      }
    }

    if (tank != null) {
      tank.draw(graphics);
    }

    super.extractRenderState(graphics, x, y, partialTicks);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    super.extractLabels(graphics, mouseX, mouseY);
    if (tank != null) {
      tank.highlightHoveredFluid(graphics, mouseX - this.leftPos, mouseY - this.topPos);
    }
  }

  @Override
  protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    super.extractTooltip(graphics, mouseX, mouseY);

    if (tank != null) {
      tank.renderTooltip(graphics, mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
    double mouseX = event.x(); double mouseY = event.y(); int button = event.button();
    assert minecraft != null && minecraft.player != null && minecraft.gameMode != null;
    if (tank != null && (button == 0 || button == 1) && !menu.getCarried().isEmpty() && !minecraft.player.isSpectator()) {
      if (tank.tryClick((int)mouseX - leftPos, (int)mouseY - topPos, button, 0)) {
        return true;
      }
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Nullable
  @Override
  public FluidLocation getFluidUnderMouse(int mouseX, int mouseY) {
    if (tank != null) {
      return tank.getFluidUnderMouse(mouseX - leftPos, mouseY - topPos);
    }
    return null;
  }
}
