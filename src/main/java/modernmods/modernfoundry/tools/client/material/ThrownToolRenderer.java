package modernmods.modernfoundry.tools.client.material;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import modernmods.modernfoundry.tools.entity.ThrownTool;
import modernmods.modernfoundry.tools.entity.ToolProjectile;

/**
 * Renderer for {@link ThrownTool}.
 * Minimal shape pending the EntityRenderer render-state system rewrite: the removed ItemRenderer/getItemRenderer no longer
 * supplies the item model, so the thrown item submission is deferred to the new item render pipeline (validated in-game).
 */
public class ThrownToolRenderer<T extends AbstractArrow & ToolProjectile> extends EntityRenderer<T, EntityRenderState> {
  public ThrownToolRenderer(Context context) {
    super(context);
  }

  @Override
  public EntityRenderState createRenderState() {
    return new EntityRenderState();
  }
}
