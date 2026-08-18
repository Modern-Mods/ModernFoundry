package modernmods.modernfoundry.library.client.armor.texture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.library.client.armor.AbstractArmorModel;
import modernmods.modernfoundry.library.client.armor.texture.ArmorTextureSupplier.ArmorTexture;

/** Armor texture which tints the texture */
@AllArgsConstructor
@Accessors(fluent = true)
@RequiredArgsConstructor
@Setter
public class TintedArmorTexture implements ArmorTexture {
  // full-bright packed lightmap (block 15, sky 15); LightTexture.pack was removed in 26.1
  private static final int MAX_LIGHT = 0xF000F0;

  private final Identifier texture;
  @Getter
  private int color = -1;
  @Getter
  private int luminosity = 0;

  public TintedArmorTexture(Identifier texture, int color) {
    this(texture, color, 0);
  }

  /** Applies luminosity to the given lightmap color. Assumes that {@code luminosity} is between 1 and 15. */
  public static int applyLuminosity(int packedLight, int luminosity) {
    // if full bright, skip some math
    if (luminosity >= 15) {
      return TintedArmorTexture.MAX_LIGHT;
    }
    // inlined version of methods from LightTexture
    return Math.max(luminosity, (packedLight & 0xFFFF) >> 4) << 4
      | Math.max(luminosity, packedLight >> 20 & 0xFFFF) << 20;
  }

  @Override
  public void renderTexture(Model model, PoseStack matrices, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, boolean hasGlint) {
    VertexConsumer buffer = ItemFeatureRenderer.getFoilBuffer(bufferSource, RenderTypes.armorCutoutNoCull(texture), false, hasGlint);
    if (luminosity > 0) {
      packedLight = applyLuminosity(packedLight, luminosity);
    }
    AbstractArmorModel.renderColored(model, matrices, buffer, packedLight, packedOverlay, color, red, green, blue, alpha);
  }
}
