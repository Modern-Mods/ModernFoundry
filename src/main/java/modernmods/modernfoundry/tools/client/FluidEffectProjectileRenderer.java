package modernmods.modernfoundry.tools.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import modernmods.mantle.client.render.FluidCuboid;
import modernmods.modernfoundry.tools.entity.FluidEffectProjectile;

import java.util.List;

// Minimal placeholder shape pending the EntityRenderer render-state system rewrite; fluid cuboid submission is deferred.
public class FluidEffectProjectileRenderer extends EntityRenderer<FluidEffectProjectile, EntityRenderState> {
  private final List<FluidCuboid> fluids;
  public FluidEffectProjectileRenderer(Context context) {
    super(context);
    this.fluids = List.of(
      FluidCuboid.builder().from(-4,  0,  0).to(-2,  2,  2).build(),
      FluidCuboid.builder().from( 0, -4,  0).to( 2, -2,  2).build(),
      FluidCuboid.builder().from( 0,  0, -4).to( 2,  2, -2).build(),
      FluidCuboid.builder().from( 2,  0,  0).to( 4,  2,  2).build(),
      FluidCuboid.builder().from( 0,  0,  0).to( 2,  4,  2).build(),
      FluidCuboid.builder().from( 0,  0,  2).to( 2,  2,  4).build());
  }

  @Override
  public EntityRenderState createRenderState() {
    return new EntityRenderState();
  }
}
