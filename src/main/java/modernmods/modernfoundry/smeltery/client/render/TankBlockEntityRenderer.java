package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.level.block.entity.BlockEntity;
import modernmods.hilt.client.render.FluidCuboid;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.RenderUtils;
import modernmods.modernfoundry.library.fluid.FluidTankAnimated;
import modernmods.modernfoundry.smeltery.block.entity.ITankBlockEntity;

import java.util.List;

public class TankBlockEntityRenderer<T extends BlockEntity & ITankBlockEntity> implements BlockEntityRenderer<T> {
  public TankBlockEntityRenderer(Context context) {}

  @Override
  public void render(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
    if (Config.CLIENT.tankFluidModel.get()) {
      return;
    }
    // render the fluid
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(tile.getBlockState(), List.of());
    if (!fluids.isEmpty()) {
      FluidTankAnimated tank = tile.getTank();
      for (FluidCuboid fluid : fluids) {
        RenderUtils.renderFluidTank(matrixStack, buffer, fluid, tank, combinedLightIn, partialTicks, true);
      }
    }
  }
}
