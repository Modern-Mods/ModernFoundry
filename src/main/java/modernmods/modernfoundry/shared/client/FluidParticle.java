package modernmods.modernfoundry.shared.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.client.render.FluidRenderer;
import modernmods.modernfoundry.shared.particle.FluidParticleData;

/** Particle type that renders a fluid still texture */
public class FluidParticle extends SingleQuadParticle {
  private final FluidStack fluid;
  private final float uCoord;
  private final float vCoord;

  /** Resolves the fluid still sprite, needed up-front as SingleQuadParticle takes the sprite in its constructor */
  private static TextureAtlasSprite stillSprite(FluidStack fluid) {
    return FluidRenderer.getFluidTextures(fluid).still();
  }

  protected FluidParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, FluidStack fluid) {
    super(world, x, y, z, motionX, motionY, motionZ, stillSprite(fluid));
    this.fluid = fluid;
    this.gravity = 1.0F;
    int color = FluidRenderer.getFluidTextures(fluid).color();
    this.alpha = ((color >> 24) & 0xFF) / 255f;
    this.rCol   = ((color >> 16) & 0xFF) / 255f;
    this.gCol = ((color >>  8) & 0xFF) / 255f;
    this.bCol  = ( color        & 0xFF) / 255f;
    this.quadSize /= 2.0F;
    this.uCoord = this.random.nextFloat() * 3.0F;
    this.vCoord = this.random.nextFloat() * 3.0F;
  }

  @Override
  public SingleQuadParticle.Layer getLayer() {
    // fluid still texture lives on the block atlas; translucent to honor the fluid tint alpha
    return SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN;
  }

  @Override
  protected float getU0() {
    return this.sprite.getU((this.uCoord + 1.0F) / 4.0F * 16.0F);
  }

  @Override
  protected float getU1() {
    return this.sprite.getU(this.uCoord / 4.0F * 16.0F);
  }

  @Override
  protected float getV0() {
    return this.sprite.getV(this.vCoord / 4.0F * 16.0F);
  }

  @Override
  protected float getV1() {
    return this.sprite.getV((this.vCoord + 1.0F) / 4.0F * 16.0F);
  }

  @Override
  public int getLightCoords(float partialTick) {
    return FluidRenderer.withBlockLight(super.getLightCoords(partialTick), fluid.getFluid().getFluidType().getLightLevel(fluid));
  }

  /** Factory to create a fluid particle */
  public static class Factory implements ParticleProvider<FluidParticleData> {
    @Override
    public Particle createParticle(FluidParticleData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
      FluidStack fluid = data.getFluid();
      return !fluid.isEmpty() ? new FluidParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, fluid) : null;
    }
  }
}
