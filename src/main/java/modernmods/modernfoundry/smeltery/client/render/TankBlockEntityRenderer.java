package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.level.block.entity.BlockEntity;
import modernmods.mantle.client.render.FluidCuboid;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.RenderUtils;
import modernmods.modernfoundry.library.fluid.FluidTankAnimated;
import modernmods.modernfoundry.smeltery.block.entity.ITankBlockEntity;

import java.util.List;

public class TankBlockEntityRenderer<T extends BlockEntity & ITankBlockEntity> implements BlockEntityRenderer<T, BlockEntityRenderState> {
  public TankBlockEntityRenderer(Context context) {}

  public BlockEntityRenderState createRenderState() {
    return new BlockEntityRenderState();
  }

  @Override
  public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    // 26.1: read the live fluid from the block entity (via the render state position) and submit the fluid cuboids as
    // custom geometry against the SubmitNodeCollector (immediate-mode rendering was removed).
    net.minecraft.world.level.Level level = net.minecraft.client.Minecraft.getInstance().level;
    if (level == null || Config.CLIENT.tankFluidModel.get()) {
      return;
    }
    net.minecraft.world.level.block.state.BlockState blockState = level.getBlockState(state.blockPos);
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(blockState, List.of());
    if (fluids.isEmpty() || !(level.getBlockEntity(state.blockPos) instanceof ITankBlockEntity tankBE)) {
      return;
    }
    FluidTankAnimated tank = tankBE.getTank();
    if (tank.getFluid().isEmpty() || tank.getCapacity() <= 0) {
      return;
    }
    int light = state.lightCoords;
    // decay the render offset toward 0 each frame so a fill/drain animates smoothly (quick, but not instant) instead of
    // snapping to the new level. Mirrors the pre-26.1 RenderUtils.renderFluidTank decay.
    final float offset = RenderUtils.decayRenderOffset(tank);
    collector.submitCustomGeometry(poseStack, modernmods.mantle.client.render.MantleRenderTypes.FLUID, (pose, buffer) -> {
      PoseStack local = new PoseStack();
      local.last().pose().set(pose.pose());
      for (FluidCuboid cube : fluids) {
        modernmods.mantle.client.render.FluidRenderer.renderScaledCuboid(local, buffer, cube, tank.getFluid(), offset, tank.getCapacity(), light, true);
      }
    });
  }
}
