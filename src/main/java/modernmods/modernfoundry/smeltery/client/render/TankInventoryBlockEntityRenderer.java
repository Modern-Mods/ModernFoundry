package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import modernmods.hilt.client.render.FluidCuboid;
import modernmods.hilt.client.render.RenderItem;
import modernmods.hilt.client.render.RenderingHelper;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.RenderUtils;
import modernmods.modernfoundry.library.fluid.FluidTankAnimated;
import modernmods.modernfoundry.smeltery.block.entity.ITankBlockEntity.ITankInventoryBlockEntity;

import java.util.List;

public class TankInventoryBlockEntityRenderer<T extends BlockEntity & ITankInventoryBlockEntity> implements BlockEntityRenderer<T> {
  private final EnumProperty<Direction> directionProperty;
  public TankInventoryBlockEntityRenderer(EnumProperty<Direction> directionProperty) {
    this.directionProperty = directionProperty;
  }

  @Override
  public void render(T melter, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int light, int combinedOverlayIn) {
    BlockState state = melter.getBlockState();
    List<FluidCuboid> fluids = Config.CLIENT.tankFluidModel.get() ? List.of() : FluidCuboid.REGISTRY.get(state, List.of());
    List<RenderItem> renderItems = RenderItem.STATE_REGISTRY.get(state, List.of());
    if (!fluids.isEmpty() || !renderItems.isEmpty()) {
      // rotate the matrix
      boolean isRotated = RenderingHelper.applyRotation(matrices, state.getValue(directionProperty));

      // render fluids
      FluidTankAnimated tank = melter.getTank();
      for (FluidCuboid fluid : fluids) {
        RenderUtils.renderFluidTank(matrices, buffer, fluid, tank, light, partialTicks, true);
      }

      // render items
      // TODO: can we show count somehow?
      for (int i = 0; i < renderItems.size(); i++) {
        RenderingHelper.renderItem(matrices, buffer, melter.getItemHandler().getStackInSlot(i), renderItems.get(i), light);
      }

      // pop back rotation
      if (isRotated) {
        matrices.popPose();
      }
    }
  }
}
