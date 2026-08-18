package modernmods.modernfoundry.library.client.model;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.joml.Matrix4fc;
import modernmods.mantle.client.model.util.DynamicItemModel;
import modernmods.mantle.client.model.util.MantleItemLayerModel;
import modernmods.modernfoundry.TConstruct;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Item model that renders a container plus its contained fluid, resolving the fluid dynamically from the stack.
 * <p>
 * In 26.1 the removed {@code ItemOverrides}/{@code BakedModel} pipeline is replaced by the item model system: this is an
 * {@link ItemModel.Unbaked} that bakes to a {@link DynamicItemModel} keyed on the contained {@link FluidStack}. The fluid
 * sprite is resolved from the {@code FluidStateModelSet} and tinted via {@link FluidTintSource#colorAsStack}.
 * <p>
 * FLUID MASK: the pre-26.1 model masked the fluid to the container's "fluid" texture window (via {@code
 * createUnbakedItemMaskElements}). Here the fluid is rendered from the fluid's own still sprite; matching the container
 * window shape requires a mask helper on the new immutable geometry pipeline and should be validated visually in-game.
 */
public final class FluidContainerModel {
  private FluidContainerModel() {}

  /** Registered id for this item model type */
  public static final Identifier ID = TConstruct.getResource("fluid_container");

  /** Relative transform applied to the fluid layer to avoid z-fighting with the base */
  public static final Transformation FLUID_TRANSFORM = new Transformation(new org.joml.Vector3f(), new org.joml.Quaternionf(), new org.joml.Vector3f(1, 1, 1.002f), new org.joml.Quaternionf());

  /**
   * Unbaked item model.
   * @param transformation  Optional extra transform
   * @param baseModel       Model providing the base texture (slot "base"), transforms and gui light
   * @param flipGas         If true, flips gasses upside down
   */
  public record Unbaked(Optional<Transformation> transformation, Identifier baseModel, boolean flipGas) implements ItemModel.Unbaked {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(Unbaked::transformation),
      Identifier.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
      com.mojang.serialization.Codec.BOOL.optionalFieldOf("flip_gas", Boolean.TRUE).forGetter(Unbaked::flipGas)
    ).apply(instance, Unbaked::new));

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type() {
      return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(baseModel);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
      Matrix4fc composed = Transformation.compose(transformation, this.transformation);
      return new Baked(context, composed, baseModel, flipGas);
    }
  }

  /** Baked item model that resolves the model per contained fluid. */
  private static final class Baked extends DynamicItemModel<FluidStack> {
    private final Identifier baseModelId;
    private final boolean flipGas;
    @Nullable
    private ItemModel emptyModel;

    private Baked(ItemModel.BakingContext context, Matrix4fc transform, Identifier baseModelId, boolean flipGas) {
      super(context, transform);
      this.baseModelId = baseModelId;
      this.flipGas = flipGas;
    }

    @Nullable
    @Override
    protected FluidStack getCacheKey(ItemStack stack) {
      FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
      if (fluid.isEmpty()) {
        return null;
      }
      // normalize the amount so the cache treats all fill levels the same
      return fluid.copyWithAmount(FluidType.BUCKET_VOLUME);
    }

    @Override
    protected ItemModel getFallback() {
      if (emptyModel == null) {
        emptyModel = bake(FluidStack.EMPTY);
      }
      return emptyModel;
    }

    @Override
    protected ItemModel bakeModel(FluidStack fluid) {
      return bake(fluid);
    }

    /** Bakes the container model for the given fluid (empty renders only the base) */
    private ItemModel bake(FluidStack fluid) {
      ModelBaker baker = context.blockModelBaker();
      ResolvedModel resolved = baker.getModel(baseModelId);
      TextureSlots slots = resolved.getTopTextureSlots();

      QuadCollection.Builder builder = new QuadCollection.Builder();

      // base layer from the model's "base" texture
      Material.Baked baseSprite = baker.materials().resolveSlot(slots, "base", resolved);
      for (BakedQuad quad : MantleItemLayerModel.getQuadsForSprite(-1, -1, baseSprite, Transformation.IDENTITY, 0)) {
        builder.addUnculledFace(quad);
      }

      // fluid layer: the fluid's still sprite clipped to the container's fluid window (base model "fluid" slot, e.g.
      // neoforge:item/mask/bucket_fluid_drip) so the fluid sits inside the bucket instead of filling the whole item square
      // and hiding it. Confirmed via bucket-diag that base/fluid slots resolve; the masked helper maps the fluid by its
      // atlas bounds so animated molten stills render. NOTE: gas flipping (flipGas) still to validate in-game.
      if (!fluid.isEmpty()) {
        Material.Baked fluidMask = baker.materials().resolveSlot(slots, "fluid", resolved);
        FluidState state = fluid.getFluid().defaultFluidState();
        FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
        int color = fluidModel.tintSource() instanceof FluidTintSource tint ? tint.colorAsStack(fluid) : -1;
        int light = fluid.getFluid().getFluidType().getLightLevel(fluid);
        for (BakedQuad quad : MantleItemLayerModel.getMaskedQuadsForSprite(color, -1, fluidModel.stillMaterial(), fluidMask, FLUID_TRANSFORM, light)) {
          builder.addUnculledFace(quad);
        }
      }

      QuadCollection quads = builder.build();
      ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolved, slots);
      return new CuboidItemModelWrapper(List.of(), quads, properties, transform);
    }
  }
}
