package modernmods.modernfoundry.library.client.modifiers.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;
import modernmods.mantle.client.model.util.ColoredBlockModel;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.model.FluidContainerModel;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.TagUtil;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Model for a fluid in a tool. */
public record FluidModifierModel(Material small, @Nullable Material large, ToolTankHelper tankHelper) implements SimpleModifierModel {
  public static final RecordLoadable<FluidModifierModel> LOADER = RecordLoadable.create(
    ModifierModel.MATERIAL_LOADABLE.requiredField("mask", FluidModifierModel::small),
    ModifierModel.MATERIAL_LOADABLE.nullableField("mask_large", FluidModifierModel::large),
    ToolTankHelper.LOADABLE.defaultField("tank_helper", ToolTankHelper.TANK_HELPER, false, FluidModifierModel::tankHelper),
    FluidModifierModel::new);

  /** Location used for baking dynamic models, name does not matter so just using a constant */
  private static final Identifier BAKE_LOCATION = TConstruct.getResource("dynamic_fluid_model");
  /**
   * The vanilla model bakery uses an orgin of 0.5,0.5,0.5, and forges dynamic fluid code uses the vanilla model bakery. (see{@link net.minecraft.client.renderer.block.model.FaceBakery} {@code #rotateVertexBy()} for vanilla bakery)
   * However, item layer wants an origin of 0,0,0, which is what we expect in our tool models. So cancel out the origin.
   */
  private static final Vector3f ORIGIN = new Vector3f(-0.5f, -0.5f, -0.5f);

  /** Instance with default tank helper */
  public FluidModifierModel(Material small, @Nullable Material large) {
    this(small, large, ToolTankHelper.TANK_HELPER);
  }

  /** Cache key for {@link #getCacheKey(IToolStackView, ModifierEntry)} */
  private record CacheKey(Fluid fluid, @Nullable CompoundTag tag) {}

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
    FluidStack fluid = tankHelper().getFluid(tool);
    if (!fluid.isEmpty()) {
      return new CacheKey(fluid.getFluid(), TagUtil.getTag(fluid));
    }
    return null;
  }

  @Override
  public RecordLoadable<FluidModifierModel> getLoader() {
    return LOADER;
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    // ensure template exists
    Material template = isLarge ? large() : small();
    if (template != null) {
      // ensure we have fluid
      FluidStack fluid = tankHelper().getFluid(tool);
      if (!fluid.isEmpty()) {
        addQuads(fluid, template, spriteGetter, transforms, quadConsumer);
      }
    }
  }

  /**
   * Adds quads for the given fluid. The {@code template} material historically acted as a mask shape that the fluid
   * texture was drawn into (via the removed {@code UnbakedGeometryHelper.createUnbakedItemMaskElements}); in 26.1 the
   * fluid is instead rendered from its own still sprite (resolved from the {@code FluidStateModelSet}) with the tint and
   * light applied. Matching the exact template mask shape requires a mask helper on the new geometry pipeline and should
   * be validated visually in-game.
   */
  public static void addQuads(FluidStack fluid, Material template, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, Consumer<Collection<BakedQuad>> quadConsumer) {
    // resolve the still sprite and tint for the fluid from the fluid model set
    net.minecraft.client.renderer.block.FluidModel fluidModel = net.minecraft.client.Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
    int color = fluidModel.tintSource() instanceof net.neoforged.neoforge.client.fluid.FluidTintSource tint ? tint.colorAsStack(fluid) : -1;
    int luminosity = fluid.getFluid().getFluidType().getLightLevel(fluid);
    Transformation transform = transforms.applyOrigin(ORIGIN).compose(FluidContainerModel.FLUID_TRANSFORM);
    List<BakedQuad> fluidQuads = modernmods.mantle.client.model.util.MantleItemLayerModel.getQuadsForSprite(color, -1, fluidModel.stillMaterial(), transform, luminosity);
    quadConsumer.accept(fluidQuads);
  }
}
