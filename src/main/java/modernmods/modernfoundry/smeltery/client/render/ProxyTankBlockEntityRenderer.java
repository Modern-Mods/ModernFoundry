package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.hilt.client.render.FluidCuboid;
import modernmods.hilt.client.render.FluidRenderer;
import modernmods.hilt.client.render.RenderItem;
import modernmods.hilt.client.render.RenderingHelper;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.smeltery.block.entity.ProxyTankBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.tank.ProxyItemTank;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/** Renderer for {@link ProxyTankBlockEntity}. Unlike {@link TankInventoryBlockEntityRenderer}, does not use a {@link modernmods.modernfoundry.library.fluid.FluidTankAnimated} */
public class ProxyTankBlockEntityRenderer implements BlockEntityRenderer<ProxyTankBlockEntity> {
  @SuppressWarnings("unused")  // nicer lambda
  public ProxyTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(ProxyTankBlockEntity proxyTank, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int light, int combinedOverlayIn) {
    BlockState state = proxyTank.getBlockState();
    List<FluidCuboid> fluids = Config.CLIENT.tankFluidModel.get() ? List.of() : FluidCuboid.REGISTRY.get(state, List.of());
    List<RenderItem> renderItems = RenderItem.STATE_REGISTRY.get(state, List.of());
    if (!fluids.isEmpty() || !renderItems.isEmpty()) {
      // rotate the matrix
      boolean isRotated = RenderingHelper.applyRotation(matrices, state.getValue(HORIZONTAL_FACING));

      // render fluids
      ProxyItemTank<?> itemTank = proxyTank.getItemTank();
      FluidStack fluid = itemTank.getFluidInTank(0);
      if (!fluids.isEmpty()) {
        int capacity = itemTank.getTankCapacity(0);
        for (FluidCuboid cube : fluids) {
          FluidRenderer.renderScaledCuboid(matrices, buffer, cube, fluid, 0, capacity, light, true);
        }
      }

      // render items
      for (int i = 0; i < renderItems.size(); i++) {
        RenderingHelper.renderItem(matrices, buffer, itemTank.getStackInSlot(i), renderItems.get(i), light);
      }

      // pop back rotation
      if (isRotated) {
        matrices.popPose();
      }
    }
  }
}
