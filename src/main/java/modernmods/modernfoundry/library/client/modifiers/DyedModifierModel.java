package modernmods.modernfoundry.library.client.modifiers;

import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import modernmods.mantle.client.model.util.MantleItemLayerModel;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.library.client.modifiers.model.SimpleModifierModel;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.nbt.IModDataView;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modifier model that copies dye from a key.
 * TODO 1.21: move to {@link modernmods.modernfoundry.library.modifiers.modules}
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class DyedModifierModel implements SimpleModifierModel {
  public static final RecordLoadable<DyedModifierModel> LOADER = SimpleModifierModel.loader(DyedModifierModel::new);
  /** @deprecated legacy system, use {@link #LOADER} */
  @Deprecated
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = (smallGetter, largeGetter) -> {
    Material smallTexture = smallGetter.apply("");
    Material largeTexture = largeGetter.apply("");
    if (smallTexture != null || largeTexture != null) {
      return new DyedModifierModel(smallTexture, largeTexture);
    }
    return null;
  };

  /** Textures to show */
  @Nullable
  private final Material small;
  @Nullable
  private final Material large;

  @Override
  public RecordLoadable<? extends DyedModifierModel> getLoader() {
    return LOADER;
  }

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry entry) {
    ModifierId modifier = entry.getId();
    IModDataView data = tool.getPersistentData();
    int color = -1;
    if (data.contains(modifier.getIdentifier())) {
      color = data.getInt(modifier.getIdentifier());
    }
    return new CacheKey(modifier, color);
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Material texture = isLarge ? large : small;
    if (texture != null) {
      IModDataView data = tool.getPersistentData();
      Identifier key = modifier.getId().getIdentifier();
      if (data.contains(key)) {
        quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(0xFF000000 | data.getInt(key), -1, new Material.Baked(spriteGetter.apply(texture), false), transforms, 0, pixels));
      }
    }
  }

  /** Data class to cache a colored texture */
  private record CacheKey(ModifierId modifier, int color) {}
}
