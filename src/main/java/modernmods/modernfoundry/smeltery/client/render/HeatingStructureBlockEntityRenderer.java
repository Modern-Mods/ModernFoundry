package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Quaternionf;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.TinkerRenderTypes;
import modernmods.modernfoundry.library.TinkerItemDisplays;
import modernmods.modernfoundry.smeltery.block.controller.ControllerBlock;
import modernmods.modernfoundry.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.module.MeltingModuleInventory;
import modernmods.modernfoundry.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

public class HeatingStructureBlockEntityRenderer implements BlockEntityRenderer<HeatingStructureBlockEntity, BlockEntityRenderState> {
  private static final float ITEM_SCALE = 15f/16f;

  public HeatingStructureBlockEntityRenderer(Context context) {}

  public BlockEntityRenderState createRenderState() {
    return new BlockEntityRenderState();
  }

  @Override
  public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    // 26.1: read the live smeltery from the block entity and submit its molten fluid volume as custom geometry. The item
    // stacks and debug error box from the pre-26.1 renderer are not yet re-wired; the molten fluid is the important part.
    Level world = Minecraft.getInstance().level;
    if (world == null || !(world.getBlockEntity(state.blockPos) instanceof HeatingStructureBlockEntity smeltery)) {
      return;
    }
    BlockState blockState = world.getBlockState(state.blockPos);
    StructureData structure = smeltery.getStructure();
    if (!blockState.getValue(ControllerBlock.IN_STRUCTURE) || structure == null) {
      return;
    }
    BlockPos pos = smeltery.getBlockPos();
    BlockPos minPos = structure.getMinInside();
    BlockPos maxPos = structure.getMaxInside();
    int light = state.lightCoords;
    poseStack.pushPose();
    poseStack.translate(minPos.getX() - pos.getX(), minPos.getY() - pos.getY(), minPos.getZ() - pos.getZ());
    collector.submitCustomGeometry(poseStack, TinkerRenderTypes.SMELTERY_FLUID, (pose, buffer) -> {
      PoseStack local = new PoseStack();
      local.last().pose().set(pose.pose());
      SmelteryTankRenderer.renderFluids(local, buffer, smeltery.getTank(), minPos, maxPos, light);
    });

    // render the melting items floating in the smeltery
    int max = Config.CLIENT.maxSmelteryItemQuads.get();
    if (max != 0) {
      int xd = 1 + maxPos.getX() - minPos.getX();
      int zd = 1 + maxPos.getZ() - minPos.getZ();
      int layer = xd * zd;
      Direction facing = blockState.getValue(ControllerBlock.FACING);
      Quaternionf itemRotation = Axis.YP.rotationDegrees(-90.0F * (float) facing.get2DDataValue());
      MeltingModuleInventory inventory = smeltery.getMeltingInventory();
      Minecraft mc = Minecraft.getInstance();
      // 26.1: ItemRenderer#getModel/#render and per-quad counting were removed with the item model rewrite. Approximate the
      // old quad budget with a flat per-item estimate so a huge smeltery still stops drawing items past the configured cap.
      int itemsRendered = 0;
      for (int i = 0; i < inventory.getSlots(); i++) {
        ItemStack stack = inventory.getStackInSlot(i);
        if (!stack.isEmpty()) {
          int height = i / layer;
          int layerIndex = i % layer;
          int offsetX = layerIndex % xd;
          int offsetZ = layerIndex / xd;
          // 26.1: per-block LevelRenderer#getLightColor changed shape; reuse the controller's packed light for the whole
          // structure (matches the fluid's lighting) rather than sampling each slot position.
          int itemLight = light;

          poseStack.pushPose();
          poseStack.translate(offsetX + 0.5f, height + 0.5f, offsetZ + 0.5f);
          poseStack.mulPose(itemRotation);
          poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
          ItemStackRenderState renderState = new ItemStackRenderState();
          mc.getItemModelResolver().updateForTopItem(renderState, stack, TinkerItemDisplays.MELTER, world, null, 0);
          renderState.submit(poseStack, collector, itemLight, OverlayTexture.NO_OVERLAY, 0);
          poseStack.popPose();

          if (max != -1) {
            itemsRendered += 50;
            if (itemsRendered > max) {
              break;
            }
          }
        }
      }
    }
    poseStack.popPose();
  }


  @Override
  public boolean shouldRenderOffScreen() {
    // 26.1.2 made shouldRenderOffScreen no-arg (per-renderer, not per-instance); always allow off-screen rendering
    // since the smeltery/foundry fluid may be visible from outside the structure. Formerly gated on IN_STRUCTURE + valid structure.
    return true;
  }
}
