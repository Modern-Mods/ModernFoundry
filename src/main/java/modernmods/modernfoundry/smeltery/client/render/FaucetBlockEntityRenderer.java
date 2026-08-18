package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import modernmods.mantle.client.render.FluidCuboid;
import modernmods.mantle.client.render.FluidRenderer;
import modernmods.mantle.client.render.MantleRenderTypes;
import modernmods.mantle.client.render.RenderingHelper;
import modernmods.modernfoundry.smeltery.block.FaucetBlock;
import modernmods.modernfoundry.smeltery.block.entity.FaucetBlockEntity;

import java.util.List;

public class FaucetBlockEntityRenderer implements BlockEntityRenderer<FaucetBlockEntity, BlockEntityRenderState> {
  public FaucetBlockEntityRenderer(Context context) {}

  @Override
  public BlockEntityRenderState createRenderState() {
    return new BlockEntityRenderState();
  }

  @Override
  public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    // 26.1: the pouring-faucet fluid stream is submitted through the SubmitNodeCollector custom-geometry path (same
    // approach as the casting table renderer) rather than the old immediate-mode MultiBufferSource.
    Level world = Minecraft.getInstance().level;
    if (world == null || !(world.getBlockEntity(state.blockPos) instanceof FaucetBlockEntity faucet)) {
      return;
    }
    FluidStack renderFluid = faucet.getRenderFluid();
    if (!faucet.isPouring() || renderFluid.isEmpty()) {
      return;
    }

    // fetch faucet model to determine where to render fluids
    BlockState blockState = world.getBlockState(state.blockPos);
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(blockState, List.of());
    if (fluids.isEmpty()) {
      return;
    }
    Direction direction = blockState.getValue(FaucetBlock.FACING);
    int light = state.lightCoords;

    // fluid textures, fetched once for both the faucet cuboids and the stream into the blocks below
    FluidRenderer.FluidTextures textures = FluidRenderer.getFluidTextures(renderFluid);
    FluidType fluidType = renderFluid.getFluid().getFluidType();
    int fluidLight = FluidRenderer.withBlockLight(light, fluidType.getLightLevel(renderFluid));

    // if on the side of a block, rotate the fluid model to match the faucet
    boolean isRotated = RenderingHelper.applyRotation(poseStack, direction);
    collector.submitCustomGeometry(poseStack, MantleRenderTypes.FLUID, (pose, buffer) -> {
      PoseStack local = new PoseStack();
      local.last().pose().set(pose.pose());
      // fluid inside the faucet itself (color/gas resolved from the stack)
      FluidRenderer.renderCuboids(local, buffer, fluids, renderFluid, light);
      // fluid falling into the block(s) below the faucet
      RenderingHelper.renderFaucetFluids(world, state.blockPos, direction, local, buffer, textures.still(), textures.flowing(), textures.color(), fluidLight);
    });
    if (isRotated) {
      poseStack.popPose();
    }
  }
}
