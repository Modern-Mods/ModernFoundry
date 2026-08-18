package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import modernmods.modernfoundry.smeltery.block.entity.GaugeBlockEntity;

/**
 * Renderer for the obisidian gauge block.
 * <p>
 * 26.1 block-entity render rewrite: the immediate-mode {@code render(T, float, PoseStack, MultiBufferSource, int, int)}
 * was replaced by the two-phase {@link #createRenderState()} + {@link #submit} pipeline. The dynamic fluid geometry
 * must be re-expressed against {@link SubmitNodeCollector} (submitCustomGeometry) using data captured into a render
 * state; exact fluid levels/position are validated in-game.
 */
public class GaugeBlockEntityRenderer implements BlockEntityRenderer<GaugeBlockEntity, BlockEntityRenderState> {
  public GaugeBlockEntityRenderer(Context context) {}

  @Override
  public BlockEntityRenderState createRenderState() {
    return new BlockEntityRenderState();
  }

  @Override
  public void submit(BlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
    // Original immediate-mode fluid render (pre-26.1), to be re-expressed against the submit pipeline:
    //   List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(tile.getBlockState(), List.of());
    //   if (!fluids.isEmpty()) {
    //     IFluidHandler tank = tile.getTank();
    //     if (tank.getTanks() > 0) {
    //       FluidStack fluid = tank.getFluidInTank(0);
    //       FluidRenderer.renderCuboids(matrices, buffer.getBuffer(MantleRenderTypes.FLUID), fluids, fluid, light);
    //     }
    //   }
  }
}
