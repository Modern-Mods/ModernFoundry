package modernmods.modernfoundry.library.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStackTemplate;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.shared.block.SlimeType;

import javax.annotation.Nullable;

// not part of the tic particle system since it uses vanilla particles
public class SlimeParticle extends BreakingItemParticle {

  public SlimeParticle(ClientLevel worldIn, double posXIn, double posYIn, double posZIn, TextureAtlasSprite sprite) {
    super(worldIn, posXIn, posYIn, posZIn, sprite);
  }

  public SlimeParticle(ClientLevel worldIn, double posXIn, double posYIn, double posZIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, TextureAtlasSprite sprite) {
    super(worldIn, posXIn, posYIn, posZIn, xSpeedIn, ySpeedIn, zSpeedIn, sprite);
  }

  // sprite resolution moved onto the provider in 26.1 (ItemParticleProvider#getSprite)
  public static class Factory extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
    private final ItemStackTemplate slime;

    public Factory(SlimeType type) {
      this.slime = new ItemStackTemplate(TinkerCommons.slimeball.get(type).asItem());
    }

    /** Creates a factory rendering the given item's break particle (e.g. terracube uses a clay ball) */
    public Factory(net.minecraft.world.item.Item item) {
      this.slime = new ItemStackTemplate(item);
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
      return new SlimeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, getSprite(slime, worldIn, random));
    }
  }
}
