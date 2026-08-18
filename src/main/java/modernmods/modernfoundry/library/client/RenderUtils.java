package modernmods.modernfoundry.library.client;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import modernmods.mantle.client.render.FluidCuboid;
import modernmods.mantle.client.render.FluidRenderer;
import modernmods.mantle.client.render.MantleRenderTypes;
import modernmods.modernfoundry.library.fluid.FluidTankAnimated;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderUtils {
  /**
   * Adds a fluid cuboid with transparency
   * @param matrices  Matrix stack instance
   * @param buffer    Render type buffer instance
   * @param fluid     Fluid to render
   * @param opacity   Fluid opacity to blend in
   * @param light     Quad lighting
   * @param cube      Fluid cuboid instance
   */
  public static void renderTransparentCuboid(PoseStack matrices, MultiBufferSource buffer, FluidCuboid cube, FluidStack fluid, int opacity, int light) {
    // nothing to render? skip
    if (opacity < 0 || fluid.isEmpty()) {
      return;
    }

    // fetch sprites and tint color from the 26.1 fluid model system (removed client-extension texture accessors)
    FluidRenderer.FluidTextures textures = FluidRenderer.getFluidTextures(fluid);
    TextureAtlasSprite still = textures.still();
    TextureAtlasSprite flowing = textures.flowing();
    FluidType fluidType = fluid.getFluid().getFluidType();
    boolean isGas = fluidType.isLighterThanAir();
    light = FluidRenderer.withBlockLight(light, fluidType.getLightLevel(fluid));

    // add in fluid opacity if given
    int color = textures.color();
    if (opacity < 0xFF) {
      // alpha is top 8 bits, multiply by opacity and divide out remainder
      int alpha = ((color >> 24) & 0xFF) * opacity / 0xFF;
      // clear bits in color and or in the new alpha
      color = (color & 0xFFFFFF) | (alpha << 24);
    }
    FluidRenderer.renderCuboid(matrices, buffer.getBuffer(MantleRenderTypes.FLUID), cube, still, flowing, cube.getFromScaled(), cube.getToScaled(), color, light, isGas);
  }

  /**
   * Decays a tank's render offset toward zero, returning the offset to render with this frame. This drives the fill/drain
   * animation: {@code updateFluidTo} seeds the offset with the fluid delta, and each rendered frame eases it back to 0 so
   * the fluid level animates smoothly (quick, but not instant) instead of snapping. Extracted from
   * {@link #renderFluidTank} for the 26.1 submit-based block-entity renderers, which lack a passed-in partial-tick.
   * @param tank  Animated tank whose offset is decayed in place
   * @return  Render offset to use this frame
   */
  public static float decayRenderOffset(FluidTankAnimated tank) {
    float offset = tank.getRenderOffset();
    if (offset > 1.2f || offset < -1.2f) {
      float partialTicks = net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
      offset = offset - ((offset / 12f + 0.1f) * partialTicks);
      tank.setRenderOffset(offset);
    } else {
      offset = 0;
      tank.setRenderOffset(0);
    }
    return offset;
  }

  /**
   * Add textured quads for a fluid tank
   * @param matrices      Matrix stack instance
   * @param buffer        Render type buffer instance
   * @param tank          Fluid tank animated to render=
   * @param light         Quad lighting
   * @param cube          Fluid cuboid instance
   * @param partialTicks  Partial ticks
   * @param flipGas       If true, flips gas cubes
   */
  public static void renderFluidTank(PoseStack matrices, MultiBufferSource buffer, FluidCuboid cube, FluidTankAnimated tank, int light, float partialTicks, boolean flipGas) {
    // render liquid if present
    FluidStack liquid = tank.getFluid();
    int capacity = tank.getCapacity();
    if (!liquid.isEmpty() && capacity > 0) {
      // update render offset
      float offset = tank.getRenderOffset();
      if (offset > 1.2f || offset < -1.2f) {
        offset = offset - ((offset / 12f + 0.1f) * partialTicks);
        tank.setRenderOffset(offset);
      } else {
        tank.setRenderOffset(0);
      }

      // fetch fluid information from the model
      FluidRenderer.renderScaledCuboid(matrices, buffer, cube, liquid, offset, capacity, light, flipGas);
    } else {
      // clear render offet if no liquid
      tank.setRenderOffset(0);
    }
  }
}
