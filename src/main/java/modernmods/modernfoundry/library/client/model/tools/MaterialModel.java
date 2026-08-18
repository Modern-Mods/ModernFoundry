package modernmods.modernfoundry.library.client.model.tools;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import modernmods.mantle.client.model.util.DynamicItemModel;
import modernmods.mantle.client.model.util.MantleItemLayerModel;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfo;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfo.TintedSprite;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfoLoader;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Model for an item with material texture variants, such as tool parts. Used only for single material items; {@link ToolModel}
 * is used for multi-material items.
 * <p>
 * In 26.1 the removed {@code ItemOverrides}/{@code BakedModel} pipeline is replaced by the item model system: this is an
 * {@link ItemModel.Unbaked} baking to a {@link DynamicItemModel} keyed on the item's {@link MaterialVariantId}.
 */
public final class MaterialModel {
  private MaterialModel() {}

  /** Registered id for this item model type */
  public static final Identifier ID = TConstruct.getResource("material");

  /** Codec for an offset pair, in pixels */
  private static final Codec<Vec2> OFFSET_CODEC = Codec.FLOAT.listOf().comapFlatMap(
    list -> list.size() == 2 ? com.mojang.serialization.DataResult.success(new Vec2(list.get(0), list.get(1))) : com.mojang.serialization.DataResult.error(() -> "Offset must have 2 values"),
    vec -> List.of(vec.x, vec.y));
  /** Codec for a material variant id */
  private static final Codec<MaterialVariantId> MATERIAL_CODEC = Codec.STRING.comapFlatMap(
    string -> {
      MaterialVariantId id = MaterialVariantId.tryParse(string);
      return id != null ? com.mojang.serialization.DataResult.success(id) : com.mojang.serialization.DataResult.error(() -> "Invalid material variant id: " + string);
    },
    MaterialVariantId::toString);


  /* Static helpers shared with {@link ToolModel} */

  /**
   * Checks that all unique material textures for the given part exist, logging any that are missing.
   */
  public static void validateMaterialTextures(TextureSlots slots, Function<Material,TextureAtlasSprite> spriteGetter, String textureName, @Nullable MaterialVariantId material) {
    Material texture = slots.getMaterial(textureName);
    if (texture == null) {
      return;
    }
    // if the texture is missing, stop here
    if (!MissingTextureAtlasSprite.getLocation().equals(texture.sprite())) {
      if (material == null) {
        MaterialRenderInfoLoader.INSTANCE.getAllRenderInfos().forEach(info -> info.getSprite(texture, spriteGetter));
      } else {
        MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material).ifPresent(info -> info.getSprite(texture, spriteGetter));
      }
    }
  }

  /** Gets the tinted sprite info for the given material */
  @SuppressWarnings("OptionalIsPresent")
  public static TintedSprite getMaterialSprite(Function<Material,TextureAtlasSprite> spriteGetter, Material texture, MaterialVariantId material) {
    Optional<MaterialRenderInfo> optional = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material);
    if (optional.isPresent()) {
      return optional.get().getSprite(texture, spriteGetter);
    }
    return new TintedSprite(spriteGetter.apply(texture), -1, 0);
  }

  /** Gets quads for the given material variant of the texture */
  public static List<BakedQuad> getQuadsForMaterial(Function<Material,TextureAtlasSprite> spriteGetter, Material texture, MaterialVariantId material, int tintIndex, Transformation transformation, @Nullable ItemLayerPixels pixels) {
    TintedSprite sprite = getMaterialSprite(spriteGetter, texture, material);
    // guard against an unstitched/absent sprite: the 26.1 quad baker throws "No sprite set" on a null sprite, which
    // would crash rendering. Skip the layer (render nothing) rather than crash if the atlas has no sprite for it.
    if (sprite.sprite() == null) {
      return List.of();
    }
    // wrap the atlas sprite as a baked material for the 26.1 quad generator
    return MantleItemLayerModel.getQuadsForSprite(sprite.color(), tintIndex, new Material.Baked(sprite.sprite(), false), transformation, sprite.emissivity(), pixels);
  }

  /** Builds the transformation applied to the quads from a pixel offset */
  public static Transformation offsetTransform(Vec2 offset) {
    if (Vec2.ZERO.equals(offset)) {
      return Transformation.IDENTITY;
    }
    // divide by 16 to convert from pixels to base values; negate Y as positive is up for transforms but down for pixels
    return new Transformation(new Vector3f(offset.x / 16, -offset.y / 16, 0), null, null, null);
  }


  /* Item model */

  /**
   * Unbaked item model.
   * @param transformation  Optional extra transform
   * @param baseModel       Model providing the "texture" material slot and transforms
   * @param material        Static material to use, or null to read the material from the stack
   * @param index           Tint index and part index
   * @param offset          Pixel offset applied to the quads
   */
  public record Unbaked(Optional<Transformation> transformation, Identifier baseModel, Optional<MaterialVariantId> material, int index, Vec2 offset) implements ItemModel.Unbaked {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(Unbaked::transformation),
      Identifier.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
      MATERIAL_CODEC.optionalFieldOf("material").forGetter(Unbaked::material),
      Codec.INT.optionalFieldOf("index", 0).forGetter(Unbaked::index),
      OFFSET_CODEC.optionalFieldOf("offset", Vec2.ZERO).forGetter(Unbaked::offset)
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
      return new Baked(context, composed, baseModel, material.orElse(null), index, offsetTransform(offset));
    }
  }

  /** Baked item model resolving the material per stack. */
  private static final class Baked extends DynamicItemModel<MaterialVariantId> {
    private final Identifier baseModelId;
    @Nullable
    private final MaterialVariantId staticMaterial;
    private final int index;
    private final Transformation offsetTransform;

    private Baked(ItemModel.BakingContext context, Matrix4fc transform, Identifier baseModelId, @Nullable MaterialVariantId staticMaterial, int index, Transformation offsetTransform) {
      super(context, transform);
      this.baseModelId = baseModelId;
      this.staticMaterial = staticMaterial;
      this.index = index;
      this.offsetTransform = offsetTransform;
    }

    @Nullable
    @Override
    protected MaterialVariantId getCacheKey(ItemStack stack) {
      if (staticMaterial != null) {
        return staticMaterial;
      }
      MaterialVariantId material = IMaterialItem.getMaterialFromStack(stack);
      return IMaterial.UNKNOWN_ID.equals(material) ? null : material;
    }

    @Override
    protected ItemModel getFallback() {
      return getModel(IMaterial.UNKNOWN_ID);
    }

    @Override
    protected ItemModel bakeModel(MaterialVariantId material) {
      ModelBaker baker = context.blockModelBaker();
      ResolvedModel resolved = baker.getModel(baseModelId);
      TextureSlots slots = resolved.getTopTextureSlots();
      Function<Material,TextureAtlasSprite> spriteGetter = mat -> baker.materials().get(mat, resolved).sprite();
      Material texture = slots.getMaterial("texture");
      QuadCollection.Builder builder = new QuadCollection.Builder();
      if (texture != null) {
        for (BakedQuad quad : getQuadsForMaterial(spriteGetter, texture, material, index, offsetTransform, null)) {
          builder.addUnculledFace(quad);
        }
      }
      QuadCollection quads = builder.build();
      ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolved, slots);
      return new CuboidItemModelWrapper(List.of(), quads, properties, transform);
    }
  }
}
