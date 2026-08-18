package modernmods.modernfoundry.library.client.armor.texture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.mantle.data.loadable.common.ColorLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modules.cosmetic.TrimModule;

import java.util.HashMap;
import java.util.Map;

/** Handles fetching textures for armor trims */
public record TrimArmorTextureSupplier(ModifierId modifier, Identifier patternKey, Identifier materialKey) implements ArmorTextureSupplier {
  /** Default instant using the tinkers modifier */
  public static TrimArmorTextureSupplier INSTANCE = new TrimArmorTextureSupplier(TinkerModifiers.trim.getId());
  public static final RecordLoadable<TrimArmorTextureSupplier> LOADER = RecordLoadable.create(ModifierId.PARSER.defaultField("modifier", TinkerModifiers.trim.getId(), TrimArmorTextureSupplier::modifier), TrimArmorTextureSupplier::new);

  /* Caches */
  private static final Map<String,ArmorTexture> ARMOR_CACHE = new HashMap<>();
  private static final Map<String,ArmorTexture> LEGGING_CACHE = new HashMap<>();
  /** Listener to clear caches associated with trim textures */
  public static final ResourceManagerReloadListener CACHE_INVALIDATOR = manager -> {
    ARMOR_CACHE.clear();
    LEGGING_CACHE.clear();
  };

  /** @apiNote use {@link #TrimArmorTextureSupplier(ModifierId)} */
  @Internal
  public TrimArmorTextureSupplier {}

  public TrimArmorTextureSupplier(ModifierId modifier) {
    this(modifier, TrimModule.patternKey(modifier), TrimModule.materialKey(modifier));
  }

  @Override
  public ArmorTexture getArmorTexture(ItemStack stack, TextureType textureType, RegistryAccess access) {
    if (textureType != TextureType.WINGS) {
      String patternId = ModifierUtil.getPersistentString(stack, patternKey);
      String materialId = ModifierUtil.getPersistentString(stack, materialKey);
      if (!patternId.isEmpty() && !materialId.isEmpty()) {
        String key = patternId + '#' + materialId;
        Map<String,ArmorTexture> cache = textureType == TextureType.LEGGINGS ? LEGGING_CACHE : ARMOR_CACHE;
        ArmorTexture texture = cache.get(key);
        if (texture != null) {
          return texture;
        }
        TrimPattern pattern = access.lookupOrThrow(Registries.TRIM_PATTERN).getOptional(Identifier.tryParse(patternId)).orElse(null);
        TrimMaterial material = access.lookupOrThrow(Registries.TRIM_MATERIAL).getOptional(Identifier.tryParse(materialId)).orElse(null);
        texture = ArmorTexture.EMPTY;
        if (pattern != null && material != null) {
          Identifier patternAsset = pattern.assetId();
          texture = TrimArmorTexture.create(patternAsset.withPath("trims/models/armor/" + patternAsset.getPath() + (textureType == TextureType.LEGGINGS ? "_leggings" : "")), material);
        }
        cache.put(key, texture);
        return texture;
      }
    }
    return ArmorTexture.EMPTY;
  }

  @Override
  public RecordLoadable<TrimArmorTextureSupplier> getLoader() {
    return LOADER;
  }

  /** Implementation of an armor texture for armor trims */
  @RequiredArgsConstructor
  public static class TrimArmorTexture implements ArmorTexture {
    private final TextureAtlasSprite trimSprite;

    /**
     * Creates the trim texture; the material-specific atlas sprite lookup is deferred.
     * <p>
     * DEFERRED RENDER: pre-26.1 resolved a per-material sprite via {@code ModelManager#getAtlas} + {@code TrimMaterial#assetName()},
     * both removed in 26.1 (the trim atlas is now owned by {@code EquipmentLayerRenderer} and the suffix lives in
     * {@code MaterialAssetGroup}). Until that renderer is driven we always tint the base texture; per-material trim sprites
     * validated in-game once re-hooked.
     */
    private static ArmorTexture create(Identifier root, TrimMaterial material) {
      int color = -1;
      TextColor textColor = material.description().getStyle().getColor();
      if (textColor != null) {
        color = textColor.getValue() | 0xFF000000;
      }
      return new TintedArmorTexture(root.withPath("textures/" + root.getPath() + ".png"), color);
    }

    @Override
    public void renderTexture(Model model, PoseStack matrices, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, boolean hasGlint) {
      // ignoring glint as odds are very low trim texture is the first one
      VertexConsumer buffer = trimSprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(false)));
      model.renderToBuffer(matrices, buffer, packedLight, packedOverlay, ARGB.color((int)(alpha * 255.0f), (int)(red * 255.0f), (int)(green * 255.0f), (int)(blue * 255.0f)));
    }
  }
}
