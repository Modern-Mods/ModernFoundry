package modernmods.modernfoundry.tools.client;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.tools.item.CrystalshotItem.CrystalshotEntity;

// Minimal placeholder shape pending the EntityRenderer render-state system rewrite; per-variant texture selection is deferred (needs a variant field on the render state).
public class CrystalshotRenderer extends ArrowRenderer<CrystalshotEntity, ArrowRenderState> {
  private static final Identifier TEXTURE = TConstruct.getResource("textures/entity/arrow/crystalshot.png");
  public CrystalshotRenderer(Context context) {
    super(context);
  }

  @Override
  public ArrowRenderState createRenderState() {
    return new ArrowRenderState();
  }

  @Override
  protected Identifier getTextureLocation(ArrowRenderState state) {
    return TEXTURE;
  }
}
