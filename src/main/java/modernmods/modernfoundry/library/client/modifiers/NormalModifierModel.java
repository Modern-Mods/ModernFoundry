package modernmods.modernfoundry.library.client.modifiers;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import modernmods.mantle.client.model.util.MantleItemLayerModel;
import modernmods.mantle.data.loadable.common.ColorLoadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.library.client.modifiers.model.SimpleModifierModel;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default modifier model loader, loads a single texture from the standard path.
 * TODO 1.21: move to {@link modernmods.modernfoundry.library.modifiers.modules}
 */
public class NormalModifierModel implements SimpleModifierModel {
  protected static final LoadableField<Integer, NormalModifierModel> COLOR_FIELD = ColorLoadable.ALPHA.defaultField("color", false, m -> m.color);
  protected static final LoadableField<Integer, NormalModifierModel> LUMINOSITY_FIELD = IntLoadable.range(0, 15).defaultField("luminosity", 0, false, m -> m.luminosity);
  public static final RecordLoadable<NormalModifierModel> LOADER = RecordLoadable.<Material,Material,Integer,Integer,NormalModifierModel>create(TEXTURE_FIELD, LARGE_TEXTURE_FIELD, COLOR_FIELD, LUMINOSITY_FIELD, NormalModifierModel::new);
  /** @deprecated legacy system, use {@link #LOADER */
  @Deprecated
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = new Unbaked(-1, 0);

  /** Textures to show */
  @Nullable
  private final Material small;
  @Nullable
  private final Material large;
  /** Color to apply to the texture */
  private final int color;
  /** Luminosity to apply to the texture */
  private final int luminosity;

  public NormalModifierModel(@Nullable Material small, @Nullable Material large, int color, int luminosity) {
    this.small = small;
    this.large = large;
    this.color = color;
    this.luminosity = luminosity;
  }

  public NormalModifierModel(@Nullable Material smallTexture, @Nullable Material largeTexture) {
    this(smallTexture, largeTexture, -1, 0);
  }

  @Nullable
  @Override
  public Material small() {
    return small;
  }

  @Nullable
  @Override
  public Material large() {
    return large;
  }

  @Override
  public RecordLoadable<? extends NormalModifierModel> getLoader() {
    return LOADER;
  }

  @Override
  public void validate(Function<Material, TextureAtlasSprite> spriteGetter) {
    if (small != null) {
      spriteGetter.apply(small);
    }
    if (large != null) {
      spriteGetter.apply(large);
    }
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry entry, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Material spriteName = isLarge ? large : small;
    if (spriteName != null) {
      quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(color, -1, new Material.Baked(spriteGetter.apply(spriteName), false), transforms, luminosity, pixels));
    }
  }

  private record Unbaked(int color, int luminosity) implements IUnbakedModifierModel {
    @Nullable
    @Override
    public IBakedModifierModel forTool(Function<String,Material> smallGetter, Function<String,Material> largeGetter) {
      Material smallTexture = smallGetter.apply("");
      Material largeTexture = largeGetter.apply("");
      if (smallTexture != null || largeTexture != null) {
        return new NormalModifierModel(smallTexture, largeTexture, color, luminosity);
      }
      return null;
    }

    @Override
    public IUnbakedModifierModel configure(JsonObject data) {
      // parse the two keys, if we ended up with something new create an instance
      int color = COLOR_FIELD.get(data);
      int luminosity = LUMINOSITY_FIELD.get(data);
      if (color != this.color || luminosity != this.luminosity) {
        return new Unbaked(color, luminosity);
      }
      return this;
    }
  }
}
