package modernmods.modernfoundry.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.inventory.InventoryMenu;

public final class YoyoRenderer extends EntityRenderer<YoyoEntity> {
  private final ItemRenderer itemRenderer;

  public YoyoRenderer(EntityRendererProvider.Context context) {
    super(context);
    itemRenderer = context.getItemRenderer();
  }

  @Override
  public void render(YoyoEntity yoyo, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
    super.render(yoyo, entityYaw, partialTick, poseStack, buffer, light);
    poseStack.pushPose();
    poseStack.translate(0.0, yoyo.getBbHeight() / 2.0, 0.0);
    poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, yoyo.yRotO, yoyo.getYRot())));
    poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F * (yoyo.tickCount % 360 + partialTick)));
    poseStack.translate(-0.5, -0.5, -0.5);
    poseStack.scale(0.5F, 0.5F, 0.5F);
    itemRenderer.renderStatic(yoyo.getYoyoStack(), ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
      poseStack, buffer, yoyo.level(), yoyo.getId());
    poseStack.popPose();
  }

  @Override
  public ResourceLocation getTextureLocation(YoyoEntity yoyo) {
    return InventoryMenu.BLOCK_ATLAS;
  }
}
