package modernmods.modernfoundry.library.client;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import modernmods.mantle.client.render.MantleRenderTypes;
import modernmods.modernfoundry.TConstruct;

import java.util.function.Consumer;

/**
 * Render types defined by Tinkers' Construct.
 * <p>
 * The 1.21.4+ engine rewrite replaced {@code CompositeState}/{@code ShaderInstance} with {@link RenderPipeline}s built
 * through {@link RenderPipelines} and {@link RenderType#create(String, RenderSetup)}. Custom pipelines are registered
 * via {@code RegisterRenderPipelinesEvent} (see {@link #registerPipelines(Consumer)}).
 */
public class TinkerRenderTypes {
  private TinkerRenderTypes() {}

  /**
   * Pipeline for the error block outline: reuses the vanilla lines shader ({@link RenderPipelines#LINES_SNIPPET}) but
   * disables backface culling and the depth test, so the outline is seen through surrounding blocks.
   */
  public static final RenderPipeline ERROR_BLOCK_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
    .withLocation(TConstruct.getResource("pipeline/error_block"))
    .withCull(false)
    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
    .build();

  /** Render type for the error block that is seen through everything, based on the vanilla lines type. */
  public static final RenderType ERROR_BLOCK = RenderType.create("modernfoundry:error_block", RenderSetup.builder(ERROR_BLOCK_PIPELINE)
    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
    .createRenderSetup());

  /**
   * Render type for smeltery fluids. Mantle's {@link MantleRenderTypes#FLUID} is already a no-cull translucent block
   * type in 26.1, so both faces of the fluid render; reuse it directly.
   */
  public static final RenderType SMELTERY_FLUID = MantleRenderTypes.FLUID;

  /** Registers Tinkers' custom render pipelines so their shaders are compiled. Call from {@code RegisterRenderPipelinesEvent}. */
  public static void registerPipelines(Consumer<RenderPipeline> registrar) {
    registrar.accept(ERROR_BLOCK_PIPELINE);
  }
}
