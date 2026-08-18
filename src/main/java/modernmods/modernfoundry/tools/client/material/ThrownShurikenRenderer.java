package modernmods.modernfoundry.tools.client.material;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.projectile.Projectile;
import modernmods.modernfoundry.tools.entity.ThrownShuriken;
import modernmods.modernfoundry.tools.entity.ToolProjectile;

/**
 * Renderer for {@link ThrownShuriken}.
 * Minimal shape pending the EntityRenderer render-state system rewrite: the removed ItemRenderer/getItemRenderer no longer
 * supplies the item model, so the spinning item submission is deferred to the new item render pipeline (validated in-game).
 */
public class ThrownShurikenRenderer<T extends Projectile & ToolProjectile> extends EntityRenderer<T, EntityRenderState> {
  public ThrownShurikenRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public EntityRenderState createRenderState() {
    return new EntityRenderState();
  }
}
