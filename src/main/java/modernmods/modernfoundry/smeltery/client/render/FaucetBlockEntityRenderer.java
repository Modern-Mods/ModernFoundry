package modernmods.modernfoundry.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import modernmods.hilt.client.render.FluidCuboid;
import modernmods.hilt.client.render.FluidRenderer;
import modernmods.hilt.client.render.RenderingHelper;
import modernmods.modernfoundry.smeltery.block.FaucetBlock;
import modernmods.modernfoundry.smeltery.block.entity.FaucetBlockEntity;
import modernmods.modernfoundry.library.client.TinkerRenderTypes;

import java.util.List;
import java.util.function.Function;

public class FaucetBlockEntityRenderer implements BlockEntityRenderer<FaucetBlockEntity> {
  public FaucetBlockEntityRenderer(Context context) {}

  @Override
  public void render(FaucetBlockEntity tileEntity, float partialTicks, PoseStack matrices, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
    FluidStack renderFluid = tileEntity.getRenderFluid();
    if (!tileEntity.isPouring() || renderFluid.isEmpty()) {
      return;
    }

    // safety
    Level world = tileEntity.getLevel();
    if (world == null) {
      return;
    }

    // fetch faucet model to determine where to render fluids
    BlockState state = tileEntity.getBlockState();
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(state, List.of());
    if (!fluids.isEmpty()) {
      // if side, rotate fluid model
      Direction direction = state.getValue(FaucetBlock.FACING);
      boolean isRotated = RenderingHelper.applyRotation(matrices, direction);

      // fluid props
      IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(renderFluid.getFluid());
      int color = attributes.getTintColor(renderFluid);
      Function<ResourceLocation, TextureAtlasSprite> spriteGetter = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
      TextureAtlasSprite still = spriteGetter.apply(attributes.getStillTexture(renderFluid));
      TextureAtlasSprite flowing = spriteGetter.apply(attributes.getFlowingTexture(renderFluid));
      FluidType fluidType = renderFluid.getFluid().getFluidType();
      combinedLightIn = FluidRenderer.withBlockLight(combinedLightIn, fluidType.getLightLevel(renderFluid));

      // render all cubes in the model
      VertexConsumer buffer = bufferIn.getBuffer(TinkerRenderTypes.SMELTERY_FLUID);
      for (FluidCuboid cube : fluids) {
        FluidRenderer.renderCuboid(matrices, buffer, cube, 0, still, flowing, color, combinedLightIn, false);
      }

      // render into the block(s) below
      RenderingHelper.renderFaucetFluids(world, tileEntity.getBlockPos(), direction, matrices, buffer, still, flowing, color, combinedLightIn);

      // if rotated, pop back rotation
      if(isRotated) {
        matrices.popPose();
      }
    }
  }
}
