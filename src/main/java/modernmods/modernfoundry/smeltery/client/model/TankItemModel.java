package modernmods.modernfoundry.smeltery.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4fc;
import modernmods.mantle.client.model.util.DynamicItemModel;
import modernmods.mantle.client.model.util.MantleItemLayerModel;
import modernmods.mantle.client.render.FluidRenderer;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.fluid.SimpleFluidResourceTank;
import modernmods.modernfoundry.smeltery.item.TankItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Item model for fluid tanks: bakes the base tank geometry, then adds a scalable fluid cuboid whose height follows the
 * stored fluid amount, textured with the fluid's still sprite. Replaces the pre-26.1 {@code TankModel} item fluid, which
 * baked the fluid through the removed {@code BakedModel}/{@code ItemOverrides} pipeline.
 */
public final class TankItemModel {
  private TankItemModel() {}

  /** Registered id for this item model type */
  public static final Identifier ID = TConstruct.getResource("tank");

  /** Number of discrete fill levels cached (finer = smoother animation, more cached models) */
  private static final int FILL_STEPS = 24;
  /** Fluid cuboid bounds in pixels (interior of the tank), height scaled by fill */
  private static final float MIN = 1.5f / 16f;
  private static final float MAX = 14.5f / 16f;
  private static final float BOTTOM = 1.5f / 16f;
  private static final float TOP = 14.5f / 16f;

  /** Unbaked tank item model, referencing the base model that supplies the tank geometry/textures/transforms. */
  public record Unbaked(Identifier base) implements ItemModel.Unbaked {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base)
    ).apply(inst, Unbaked::new));

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(base);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
      return new Baked(context, transform, base);
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type() {
      return MAP_CODEC;
    }
  }

  /** Cache key: the fluid plus a discrete fill level */
  private record CacheKey(Fluid fluid, int fill) {}

  /** Baked model, caching a composite (base + fluid) per fluid and fill level. */
  private static final class Baked extends DynamicItemModel<CacheKey> {
    private final Identifier base;
    // lazily resolved base data, stable for the captured context
    @Nullable private QuadCollection baseQuads;
    @Nullable private ModelRenderProperties properties;
    @Nullable private ItemModel emptyModel;

    private Baked(ItemModel.BakingContext context, Matrix4fc transform, Identifier base) {
      super(context, transform);
      this.base = base;
    }

    private void ensureResolved() {
      if (baseQuads == null) {
        ModelBaker baker = context.blockModelBaker();
        ResolvedModel resolved = baker.getModel(base);
        TextureSlots slots = resolved.getTopTextureSlots();
        baseQuads = resolved.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);
        properties = ModelRenderProperties.fromResolvedModel(baker, resolved, slots);
      }
    }

    @Nullable
    @Override
    protected CacheKey getCacheKey(ItemStack stack) {
      SimpleFluidResourceTank tank = TankItem.getTank(stack, 1);
      FluidStack fluid = tank.getFluid();
      if (fluid.isEmpty() || tank.getCapacity() <= 0) {
        return null;
      }
      int fill = Math.max(1, Math.min(FILL_STEPS, Math.round(FILL_STEPS * fluid.getAmount() / (float) tank.getCapacity())));
      return new CacheKey(fluid.getFluid(), fill);
    }

    @Override
    protected ItemModel getFallback() {
      // empty tank: base geometry only
      if (emptyModel == null) {
        ensureResolved();
        emptyModel = new CuboidItemModelWrapper(List.of(), Objects.requireNonNull(baseQuads), properties, transform);
      }
      return emptyModel;
    }

    @Override
    protected ItemModel bakeModel(CacheKey key) {
      ensureResolved();
      QuadCollection.Builder builder = new QuadCollection.Builder();
      for (var quad : Objects.requireNonNull(baseQuads).getAll()) {
        builder.addUnculledFace(quad);
      }
      // add the fluid cuboid, height scaled by fill level
      FluidRenderer.FluidTextures textures = FluidRenderer.getFluidTextures(new FluidStack(key.fluid(), 1));
      Material.Baked material = new Material.Baked(textures.still(), false);
      float top = BOTTOM + (TOP - BOTTOM) * (key.fill() / (float) FILL_STEPS);
      addFluidBox(builder, material, textures.still(), textures.color(), MIN, BOTTOM, MIN, MAX, top, MAX);
      return new CuboidItemModelWrapper(List.of(), builder.build(), properties, transform);
    }

    /** Bakes the six faces of a solid fluid box textured with the still sprite mapped fully across each face. */
    private static void addFluidBox(QuadCollection.Builder builder, Material.Baked material, TextureAtlasSprite sprite, int color,
                                    float x0, float y0, float z0, float x1, float y1, float z1) {
      QuadBakingVertexConsumer baker = new QuadBakingVertexConsumer();
      float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
      // south (+Z)
      builder.addUnculledFace(MantleItemLayerModel.buildQuad(baker, baker, material, -1, Direction.SOUTH, color, 0,
        x0, y0, z1, u0, v1, x1, y0, z1, u1, v1, x1, y1, z1, u1, v0, x0, y1, z1, u0, v0));
      // north (-Z)
      builder.addUnculledFace(MantleItemLayerModel.buildQuad(baker, baker, material, -1, Direction.NORTH, color, 0,
        x0, y0, z0, u0, v1, x0, y1, z0, u0, v0, x1, y1, z0, u1, v0, x1, y0, z0, u1, v1));
      // up (+Y)
      builder.addUnculledFace(MantleItemLayerModel.buildQuad(baker, baker, material, -1, Direction.UP, color, 0,
        x0, y1, z1, u0, v1, x1, y1, z1, u1, v1, x1, y1, z0, u1, v0, x0, y1, z0, u0, v0));
      // down (-Y)
      builder.addUnculledFace(MantleItemLayerModel.buildQuad(baker, baker, material, -1, Direction.DOWN, color, 0,
        x0, y0, z0, u0, v0, x1, y0, z0, u1, v0, x1, y0, z1, u1, v1, x0, y0, z1, u0, v1));
      // west (-X)
      builder.addUnculledFace(MantleItemLayerModel.buildQuad(baker, baker, material, -1, Direction.WEST, color, 0,
        x0, y0, z0, u0, v1, x0, y0, z1, u1, v1, x0, y1, z1, u1, v0, x0, y1, z0, u0, v0));
      // east (+X)
      builder.addUnculledFace(MantleItemLayerModel.buildQuad(baker, baker, material, -1, Direction.EAST, color, 0,
        x1, y0, z1, u0, v1, x1, y0, z0, u1, v1, x1, y1, z0, u1, v0, x1, y1, z1, u0, v0));
    }
  }
}
