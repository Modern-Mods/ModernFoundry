package modernmods.modernfoundry.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import modernmods.hilt.client.render.FluidCuboid;
import modernmods.hilt.client.render.FluidRenderer;
import modernmods.modernfoundry.tools.entity.FluidEffectProjectile;
import modernmods.modernfoundry.library.client.TinkerRenderTypes;

import java.util.List;

public class FluidEffectProjectileRenderer extends EntityRenderer<FluidEffectProjectile> {
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
  public void render(FluidEffectProjectile pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
    pMatrixStack.pushPose();
    pMatrixStack.translate(0.0D, 0.15F, 0.0D);
    pMatrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
    pMatrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot())));
    FluidRenderer.renderCuboids(pMatrixStack, pBuffer.getBuffer(TinkerRenderTypes.SMELTERY_FLUID), fluids, pEntity.getFluid(), pPackedLight);
    pMatrixStack.popPose();
    super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
  }

  @Override
  public ResourceLocation getTextureLocation(FluidEffectProjectile pEntity) {
    return InventoryMenu.BLOCK_ATLAS;
  }
}
