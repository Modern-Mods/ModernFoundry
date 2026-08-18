package modernmods.modernfoundry.gadgets.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.core.BlockPos;
import modernmods.modernfoundry.gadgets.entity.FancyItemFrameEntity;
import modernmods.modernfoundry.gadgets.entity.FrameType;

// 26.1.2: item-frame RenderState overhaul (render/getRenderOffset/shouldShowName/renderNameTag replaced by submit + ItemFrameRenderState,
// and the ModelResourceLocation / ModelEvent.RegisterAdditional standalone-model API was replaced by StandaloneModelKey/UnbakedStandaloneModel) --
// custom frame-model rendering stubbed to the vanilla item-frame renderer for a later render pass; the extra-block-light behavior is preserved below.
public class FancyItemFrameRenderer<T extends FancyItemFrameEntity> extends ItemFrameRenderer<T> {
  public FancyItemFrameRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  protected int getBlockLightLevel(T frame, BlockPos pPos) {
    int baseLight = super.getBlockLightLevel(frame, pPos);
    return frame.getFrameType() == FrameType.MANYULLYN ? Math.max(7, baseLight) : baseLight;
  }
}
