package modernmods.modernfoundry.library.client.modifiers.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import modernmods.mantle.client.model.util.MantleItemLayerModel;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfo;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.nbt.IModDataView;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.cosmetic.BannerModule;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Modifier model that renders all banner patterns on a tool */
public record BannerModifierModel(@Nullable Identifier smallPrefix, @Nullable Identifier largePrefix) implements ModifierModel {
  public static final RecordLoadable<BannerModifierModel> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.nullableField("prefix", BannerModifierModel::smallPrefix),
    Loadables.RESOURCE_LOCATION.nullableField("prefix_large", BannerModifierModel::largePrefix),
    BannerModifierModel::new);

  @Override
  public RecordLoadable<? extends ModifierModel> getLoader() {
    return LOADER;
  }

  @Override
  public void validate(Function<Material, TextureAtlasSprite> spriteGetter) {
    // since these are dynamically loaded, condition based on the config option
    if (Config.CLIENT.logMissingModifierTextures.get()) {
      for (Identifier id : BannerModule.VANILLA_PATTERN_IDS) {
        String suffix = MaterialRenderInfo.getSuffix(id);
        if (smallPrefix != null) {
          spriteGetter.apply(ModifierModel.blockAtlas(smallPrefix.withSuffix(suffix)));
        }
        if (largePrefix != null) {
          spriteGetter.apply(ModifierModel.blockAtlas(largePrefix.withSuffix(suffix)));
        }
      }
    }
  }

  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
    return tool.getPersistentData().getInt(BannerModule.cacheKey(modifier.getId()));
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Identifier prefix = isLarge ? largePrefix : smallPrefix;
    if (prefix != null) {
      IModDataView modData = tool.getPersistentData();
      Identifier key = BannerModule.patternKey(modifier.getId());
      if (modData.contains(key)) {
        ListTag list = modData.getList(key, net.minecraft.nbt.Tag.TAG_COMPOUND);
        List<BakedQuad> quads = new ArrayList<>(list.size());
        // iterate all patterns
        for (int i = 0; i < list.size(); i++) {
          // patterns are stored as short strings for some reason, for consistency we also store as hashes
          // map that back to the pattern
          CompoundTag tag = list.getCompoundOrEmpty(i);
          Identifier pattern = BannerModule.patternId(tag.getStringOr(BannerModule.KEY_PATTERN, ""));
          int color = tag.getIntOr(BannerModule.KEY_COLOR, 0);
          if (pattern != null) {
            TextureAtlasSprite sprite = spriteGetter.apply(ModifierModel.blockAtlas(prefix.withSuffix(MaterialRenderInfo.getSuffix(pattern))));
            // skip if sprite is missing - deals with modded patterns that we haven't made textures for
            if (!MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
              quads.add(MantleItemLayerModel.getQuadForGui(color, -1, new Material.Baked(sprite, false), transforms, 0));
            }
          }
        }
        if (!quads.isEmpty()) {
          quadConsumer.accept(quads);
        }
      }
    }
  }
}
