package modernmods.modernfoundry.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Layer that renders a helmet/skull worn by a slime entity.
 */
// 26.1.2: client render overhaul — RenderLayer was rebuilt around EntityRenderState + the submit pipeline
// (RenderLayer<S extends EntityRenderState, M extends EntityModel<? super S>> with a single
// submit(PoseStack, SubmitNodeCollector, int, S, float, float) hook). The old render() body relied on
// removed APIs: HierarchicalModel, SkullBlockRenderer#createSkullRenderers/#renderSkull,
// RenderType#armorCutoutNoCull, ItemInHandRenderer#renderItem and direct ArmorItem access — all of which
// need the render-state extract/submit refactor. This is a minimal correct-shaped stub; the helmet/skull
// drawing must be reimplemented against the new SlimeRenderState during the render pass.
public class SlimeArmorLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
  public SlimeArmorLayer(RenderLayerParent<S, M> parent) {
    super(parent);
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, S state, float yRot, float xRot) {
    // helmet + skull rendering pending the render-state refactor described above
  }
}
