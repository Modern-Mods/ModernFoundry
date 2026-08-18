package modernmods.modernfoundry.world.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.TConstruct;

// 26.1.2: client render overhaul — MobRenderer is now MobRenderer<T, S extends LivingEntityRenderState, M>
// and renders through the EntityRenderState/submit pipeline. The old renderer used LavaSlimeModel (removed,
// now MagmaCubeModel), a custom squish scale() override (signature changed to scale(RenderState, PoseStack))
// and a SlimeArmorLayer. Minimal correct-shaped stub extending SlimeRenderer so the entity still renders; a
// later render pass should restore the magma-cube model, the size-based squish scale and the armor layer.
public class TerracubeRenderer extends SlimeRenderer {
  private static final Identifier TEXTURE = TConstruct.getResource("textures/entity/terracube.png");

  public TerracubeRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public Identifier getTextureLocation(SlimeRenderState state) {
    return TEXTURE;
  }
}
