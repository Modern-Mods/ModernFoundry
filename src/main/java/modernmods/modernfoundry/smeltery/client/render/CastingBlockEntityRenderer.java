package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.client.render.FluidCuboid;
import modernmods.mantle.client.render.FluidRenderer;
import modernmods.mantle.client.render.RenderItem;
import modernmods.mantle.client.render.RenderingHelper;
import modernmods.modernfoundry.library.client.RenderUtils;
import modernmods.modernfoundry.smeltery.block.entity.CastingBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.tank.CastingFluidHandler;
import modernmods.modernfoundry.smeltery.client.util.CastingItemRenderTypeBuffer;

import java.util.List;

public class CastingBlockEntityRenderer implements BlockEntityRenderer<CastingBlockEntity, BlockEntityRenderState> {
  public CastingBlockEntityRenderer(Context context) {}

  public BlockEntityRenderState createRenderState() {
    return new BlockEntityRenderState();
  }

  @Override
  public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    // 26.1: render the casting fluid and the cast/output items from the live block entity via the submit-node pipeline.
    net.minecraft.world.level.Level world = net.minecraft.client.Minecraft.getInstance().level;
    if (world == null || !(world.getBlockEntity(state.blockPos) instanceof CastingBlockEntity casting)) {
      return;
    }
    BlockState blockState = world.getBlockState(state.blockPos);
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(blockState, List.of());
    List<RenderItem> renderItems = RenderItem.STATE_REGISTRY.get(blockState, List.of());
    if (fluids.isEmpty() && renderItems.isEmpty()) {
      return;
    }
    int light = state.lightCoords;
    boolean isRotated = RenderingHelper.applyRotation(poseStack, blockState);

    // render fluids
    if (!fluids.isEmpty()) {
      CastingFluidHandler tank = casting.getTank();
      FluidStack fluidStack = tank.getFluid();
      int capacity = tank.getCapacity();
      if (!fluidStack.isEmpty() && capacity > 0) {
        collector.submitCustomGeometry(poseStack, modernmods.mantle.client.render.MantleRenderTypes.FLUID, (pose, buffer) -> {
          PoseStack local = new PoseStack();
          local.last().pose().set(pose.pose());
          for (FluidCuboid cube : fluids) {
            FluidRenderer.renderScaledCuboid(local, buffer, cube, fluidStack, 0, capacity, light, false);
          }
        });
      }
    }

    // render items (input cast + output). Opacity fading from the pre-26.1 renderer is dropped; the item submit path
    // resolves the item model directly without a tint/opacity buffer wrapper.
    if (!renderItems.isEmpty()) {
      // input cast is drawn as-is
      RenderingHelper.renderItem(poseStack, collector, casting.getItem(0), renderItems.get(0), light);
      // output may be the recipe output instead of the current item
      if (renderItems.size() >= 2) {
        RenderItem outputModel = renderItems.get(1);
        if (!outputModel.isHidden()) {
          ItemStack output = casting.getItem(1);
          if (output.isEmpty()) {
            output = casting.getRecipeOutput();
          }
          RenderingHelper.renderItem(poseStack, collector, output, outputModel, light);
        }
      }
    }

    if (isRotated) {
      poseStack.popPose();
    }
  }

}
