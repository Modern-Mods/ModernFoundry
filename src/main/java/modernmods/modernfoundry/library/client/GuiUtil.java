package modernmods.modernfoundry.library.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.client.render.FluidRenderer;
import modernmods.mantle.client.screen.ElementScreen;
import modernmods.modernfoundry.library.recipe.partbuilder.Pattern;

/**
 * 26.1 GUI note: rendering moved from GuiGraphics to the mesh-based GuiGraphicsExtractor.
 * Color is threaded through the blit/blitSprite color argument (RenderSystem.setShaderColor and
 * the manual color/depth masks were removed in the GPU rewrite). Exact fluid tiling and the
 * upside-down (gas) flip are runtime-visual details approximated here via blitSprite.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GuiUtil {
  /**
   * Draws the background of a container
   * @param graphics    Graphics context
   * @param screen      Parent screen
   * @param background  Background location
   */
  public static void drawBackground(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, Identifier background) {
    // container backgrounds are the standard 256x256 texture sheet
    graphics.blit(RenderPipelines.GUI_TEXTURED, background, screen.getGuiLeft(), screen.getGuiTop(), 0f, 0f, screen.getXSize(), screen.getYSize(), 256, 256);
  }

  /**
   * Checks if the given area is hovered
   * @param mouseX    Mouse X position
   * @param mouseY    Mouse Y position
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @return  True if the area is hovered
   */
  public static boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
    return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
  }

  /**
   * Checks if the given tank area is hovered
   * @param mouseX    Mouse X position
   * @param mouseY    Mouse Y position
   * @param amount    Current tank amount
   * @param capacity  Tank capacity
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @return  True if the tank is hovered, false otherwise
   */
  public static boolean isTankHovered(int mouseX, int mouseY, int amount, int capacity, int x, int y, int width, int height) {
    // check X position first, its easier
    if (mouseX < x || mouseX > x + width || mouseY > y + height) {
      return false;
    }
    // next, try height
    int topHeight = height - (height * amount / capacity);
    return mouseY > y + topHeight;
  }

  /**
   * Renders a fluid tank with a partial fluid level
   * @param screen    Parent screen
   * @param stack     Fluid stack
   * @param capacity  Tank capacity, determines height
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @param depth     Tank depth
   */
  public static void renderFluidTank(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, FluidStack stack, int capacity, int x, int y, int width, int height, int depth) {
    renderFluidTank(graphics, screen, stack, stack.getAmount(), capacity, x, y, width, height, depth);
  }

  /**
   * Renders a fluid tank with a partial fluid level and an amount override
   * @param screen    Parent screen
   * @param stack     Fluid stack
   * @param capacity  Tank capacity, determines height
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @param depth     Tank depth
   */
  public static void renderFluidTank(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, FluidStack stack, int amount, int capacity, int x, int y, int width, int height, int depth) {
    if(!stack.isEmpty() && capacity > 0) {
      int maxY = y + height;
      int fluidHeight = Math.min(height * amount / capacity, height);
      renderTiledFluid(graphics, screen, stack, x, maxY - fluidHeight, width, fluidHeight, depth);
    }
  }

  /**
   * Colors and renders a fluid sprite
   * @param screen  Parent screen
   * @param stack   Fluid stack
   * @param x       Fluid X
   * @param y       Fluid Y
   * @param width   Fluid width
   * @param height  Fluid height
   * @param depth   Fluid depth
   */
  public static void renderTiledFluid(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, FluidStack stack, int x, int y, int width, int height, int depth) {
    if (!stack.isEmpty()) {
      // 26.1: fluid sprites/tint now come from the FluidStateModelSet via Mantle's helper
      FluidRenderer.FluidTextures textures = FluidRenderer.getFluidTextures(stack);
      int color = textures.color() | 0xFF000000;
      renderTiledTextureAtlas(graphics, screen, textures.still(), x, y, width, height, depth, stack.getFluid().getFluidType().isLighterThanAir(), color);
    }
  }

  /**
   * Renders a texture atlas sprite tiled over the given area
   * @param screen      Parent screen
   * @param sprite      Sprite to render
   * @param x           X position to render
   * @param y           Y position to render
   * @param width       Render width
   * @param height      Render height
   * @param depth       Render depth (unused in the 26.1 mesh renderer, kept for signature compatibility)
   * @param upsideDown  If true, flips the sprite (approximated; gas flip is a runtime-visual detail)
   * @param color       Tint color, ARGB
   */
  public static void renderTiledTextureAtlas(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, TextureAtlasSprite sprite, int x, int y, int width, int height, int depth, boolean upsideDown, int color) {
    int spriteWidth = sprite.contents().width();
    int spriteHeight = sprite.contents().height();
    int startX = x + screen.getGuiLeft();
    int startY = y + screen.getGuiTop();
    // tile the sprite over the area; the 26.1 mesh renderer applies tint via the blitSprite color argument
    int remainingHeight = height;
    int drawY = startY;
    while (remainingHeight > 0) {
      int renderHeight = Math.min(spriteHeight, remainingHeight);
      int remainingWidth = width;
      int drawX = startX;
      while (remainingWidth > 0) {
        int renderWidth = Math.min(spriteWidth, remainingWidth);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, drawX, drawY, renderWidth, renderHeight, color);
        drawX += renderWidth;
        remainingWidth -= renderWidth;
      }
      drawY += renderHeight;
      remainingHeight -= renderHeight;
    }
  }

  /**
   * Draws an upwards progress bar
   * @param element   Element to draw
   * @param x         X position to start
   * @param y         Y position to start
   * @param progress  Progress between 0 and 1
   */
  public static void drawProgressUp(GuiGraphicsExtractor graphics, ElementScreen element, int x, int y, float progress) {
    int height;
    if (progress > 1) {
      height = element.h;
    } else if (progress < 0) {
      height = 0;
    } else {
      // add an extra 0.5 so it rounds instead of flooring
      height = (int)(progress * element.h + 0.5);
    }
    // amount to offset element by for the height
    int deltaY = element.h - height;
    graphics.blit(RenderPipelines.GUI_TEXTURED, element.texture, x, y + deltaY, (float)element.x, (float)(element.y + deltaY), element.w, height, element.texW, element.texH);
  }

  /**
   * Renders a highlight overlay for the given area
   * @param graphics  Graphics instance
   * @param x         Element X position
   * @param y         Element Y position
   * @param width     Element width
   * @param height    Element height
   */
  public static void renderHighlight(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
    // 26.1: color/depth masks removed; the translucent white fill provides the highlight
    graphics.fill(x, y, x + width, y + height, 0x80FFFFFF);
  }

  /** Renders a pattern at the given location */
  public static void renderPattern(GuiGraphicsExtractor graphics, Pattern pattern, int x, int y) {
    TextureAtlasSprite sprite = FluidRenderer.getBlockSprite(pattern.getTexture());
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
  }
}
