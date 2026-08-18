package modernmods.modernfoundry.tools.client.material;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.armor.texture.ArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.TintedArmorTexture;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfo;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfoLoader;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.utils.SimpleCache;
import modernmods.modernfoundry.tools.entity.CombatFishingHook;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Renderer for {@link CombatFishingHook}. Mostly a recreation of vanilla's fishing hook renderer.
 * Minimal placeholder shape pending the EntityRenderer render-state system rewrite; the material texture cache is retained but the geometry submission is deferred.
 */
public class CombatFishingHookRenderer extends EntityRenderer<CombatFishingHook, EntityRenderState> {
  /** Texture under the local folder */
  private static final Identifier LOCAL = TConstruct.getResource("fishing_hook/material");
  /** Easier to append material variants without root */
  private static final Identifier BASE = ArmorTextureSupplier.getTexturePath(LOCAL);

  /** Checks if the given texture is valid */
  @Nullable
  private static Identifier tryTexture(String material) {
    Identifier texture = LOCAL.withSuffix(material);
    if (ArmorTextureSupplier.TEXTURE_VALIDATOR.test(texture)) {
      return ArmorTextureSupplier.getTexturePath(texture);
    }
    return null;
  }

  /** Cache of texture and color for each material. */
  private static final SimpleCache<MaterialVariantId,MaterialTexture> TEXTURE_CACHE = new SimpleCache<>(material -> {
    if (!IMaterial.UNKNOWN_ID.equals(material)) {
      Optional<MaterialRenderInfo> infoOptional = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material);
      int color = -1;
      int luminosity = 0;
      if (infoOptional.isPresent()) {
        MaterialRenderInfo info = infoOptional.get();
        // first try untinted
        Identifier untinted = info.texture();
        luminosity = info.luminosity();
        if (untinted != null) {
          Identifier texture = tryTexture('_' + untinted.getNamespace() + '_' + untinted.getPath());
          if (texture != null) {
            return new MaterialTexture(texture, -1, luminosity);
          }
        }
        // fallback to tinted
        color = info.vertexColor();
        for (String fallback : info.fallbacks()) {
          Identifier texture = tryTexture('_' + fallback);
          if (texture != null) {
            return new MaterialTexture(texture, color, luminosity);
          }
        }
      }
      // tint base texture
      return new MaterialTexture(ArmorTextureSupplier.getTexturePath(LOCAL), color, luminosity);
    }
    return MaterialTexture.EMPTY;
  });

  public CombatFishingHookRenderer(Context context) {
    super(context);
  }

  /** Clears any cache in the renderer. */
  public static void clearCache() {
    TEXTURE_CACHE.clear();
  }

  @Override
  public EntityRenderState createRenderState() {
    return new EntityRenderState();
  }

  private record MaterialTexture(RenderType texture, int luminosity, int alpha, int red, int green, int blue) {
    public static final MaterialTexture EMPTY = new MaterialTexture(BASE, -1, 0);

    public MaterialTexture(Identifier texture, int color, int luminosity) {
      this(RenderTypes.entityCutout(texture), luminosity,
        color >> 24 & 255,
        color >> 16 & 255,
        color >> 8 & 255,
        color & 255
      );
    }

    /** Applies luminosity to the given lightmap color */
    public int applyLuminosity(int packedLight) {
      if (luminosity > 0) {
        return TintedArmorTexture.applyLuminosity(packedLight, luminosity);
      }
      return packedLight;
    }

    /** Draws a vertex using this texture. */
    public void vertex(VertexConsumer consumer, PoseStack.Pose pose, int lightmap, float pX, int pY, int pU, int pV) {
      consumer.addVertex(pose, pX - 0.5f, pY - 0.5f, 0f)
        .setColor(red, green, blue, alpha)
        .setUv(pU, pV)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(lightmap)
        .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
  }
}
