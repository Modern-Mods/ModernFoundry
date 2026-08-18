package modernmods.modernfoundry.library.client.modifiers;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
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
 * Modifier model that renders the textured tinted based on the active potion color.
 * TODO 1.21: move to {@link modernmods.modernfoundry.library.modifiers.modules}
 */
public class PotionModifierModel implements SimpleModifierModel {
  public static final RecordLoadable<PotionModifierModel> LOADER = SimpleModifierModel.loader(PotionModifierModel::new);
  /** @deprecated legacy system, use {@link #LOADER} */
  @Deprecated
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = (smallGetter, largeGetter) -> {
    Material smallTexture = smallGetter.apply("");
    Material largeTexture = largeGetter.apply("");
    if (smallTexture != null || largeTexture != null) {
      return new PotionModifierModel(smallTexture, largeTexture);
    }
    return null;
  };

  /** Textures to show */
  @Nullable
  private final Material small;
  @Nullable
  private final Material large;

  public PotionModifierModel(@Nullable Material small, @Nullable Material large) {
    this.small = small;
    this.large = large;
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
  public RecordLoadable<? extends PotionModifierModel> getLoader() {
    return LOADER;
  }

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry entry) {
    ModifierId modifier = entry.getId();
    return new CacheKey(modifier, tool.getPersistentData().getString(modifier.getIdentifier()));
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Material texture = isLarge ? large : small;
    if (texture != null) {
      Identifier key = modifier.getId().getIdentifier();
      IModDataView toolData = tool.getPersistentData();
      if (toolData.contains(key)) {
        Identifier id = Identifier.tryParse(toolData.getString(key));
        if (id != null) {
          BuiltInRegistries.POTION.get(id).ifPresent(holder ->
            quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(0xFF000000 | PotionUtils.getColor(holder), -1, new Material.Baked(spriteGetter.apply(texture), false), transforms, 0, pixels)));
        }
      }
    }
  }

  /** Data class to cache a colored texture */
  private record CacheKey(ModifierId modifier, String potion) {}
}
