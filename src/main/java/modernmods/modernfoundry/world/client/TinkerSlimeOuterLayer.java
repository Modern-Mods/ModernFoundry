package modernmods.modernfoundry.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Copy of vanilla {@link net.minecraft.client.renderer.entity.layers.SlimeOuterLayer} that draws the translucent outer
 * shell with the mod's slime texture instead of the hardcoded vanilla {@code SlimeRenderer.SLIME_LOCATION}. The outer
 * shell is the large translucent cube that dominates the slime's appearance, so without this the Tinkers slimes rendered
 * with a vanilla-green shell over their colored inner cube.
 */
public class TinkerSlimeOuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
  private final SlimeModel model;
  private final Identifier texture;

  public TinkerSlimeOuterLayer(RenderLayerParent<SlimeRenderState, SlimeModel> renderer, EntityModelSet modelSet, Identifier texture) {
    super(renderer);
    this.model = new SlimeModel(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
    this.texture = texture;
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, SlimeRenderState state, float yRot, float xRot) {
    boolean appearsGlowingWithInvisibility = state.appearsGlowing() && state.isInvisible;
    if (!state.isInvisible || appearsGlowingWithInvisibility) {
      int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
      if (appearsGlowingWithInvisibility) {
        submitNodeCollector.order(1)
          .submitModel(this.model, state, poseStack, RenderTypes.outline(this.texture), lightCoords, overlayCoords, state.outlineColor, null);
      } else {
        submitNodeCollector.order(1)
          .submitModel(this.model, state, poseStack, RenderTypes.entityTranslucent(this.texture), lightCoords, overlayCoords, state.outlineColor, null);
      }
    }
  }
}
