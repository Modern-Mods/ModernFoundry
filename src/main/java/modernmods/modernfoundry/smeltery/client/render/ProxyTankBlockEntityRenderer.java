package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.client.render.FluidCuboid;
import modernmods.mantle.client.render.FluidRenderer;
import modernmods.mantle.client.render.RenderItem;
import modernmods.mantle.client.render.RenderingHelper;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.smeltery.block.entity.ProxyTankBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.tank.ProxyItemTank;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/** Renderer for {@link ProxyTankBlockEntity}. Unlike {@link TankInventoryBlockEntityRenderer}, does not use a {@link modernmods.modernfoundry.library.fluid.FluidTankAnimated} */
public class ProxyTankBlockEntityRenderer implements BlockEntityRenderer<ProxyTankBlockEntity, BlockEntityRenderState> {
  @SuppressWarnings("unused")  // nicer lambda
  public ProxyTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  public BlockEntityRenderState createRenderState() {
    return new BlockEntityRenderState();
  }

  @Override
  public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    // 26.1 BER rewrite: immediate-mode render replaced by extractRenderState + submit. The block-entity geometry
    // (dynamic fluid/items) must be captured into a render state and re-expressed against SubmitNodeCollector;
    // exact fluid levels/positions are validated in-game. Original immediate-mode logic preserved for re-wiring:
    /*
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
  */
  }
}
