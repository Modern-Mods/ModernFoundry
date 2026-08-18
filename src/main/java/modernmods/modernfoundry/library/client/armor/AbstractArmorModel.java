package modernmods.modernfoundry.library.client.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.library.client.armor.texture.ArmorTextureSupplier.ArmorTexture;
import modernmods.modernfoundry.library.client.armor.texture.ArmorTextureSupplier.TextureType;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.item.armor.ModifiableArmorItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Common shared logic for material armor models.
 * <p>
 * In 26.1 {@link Model} became generic over a render state and {@code Model#renderToBuffer} is now final (it renders the
 * model's own root {@link ModelPart}); the pre-26.1 approach of overriding renderToBuffer to draw custom armor texture
 * layers no longer works and armor rendering moved to the equipment-layer system. This base carries no geometry (an empty
 * root) and keeps the shared setup/color helpers; the custom material-layer drawing and elytra wing alignment are validated
 * in-game once re-hooked onto the equipment-layer renderer.
 */
public abstract class AbstractArmorModel extends Model<HumanoidRenderState> {
  /** Base model instance for rendering */
  @Nullable
  protected HumanoidModel<?> base;
  /** If true, applies the enchantment glint to extra layers */
  protected boolean hasGlint = false;
  /** If true, uses the legs texture */
  protected TextureType textureType = TextureType.ARMOR;

  protected boolean hasWings = false;

  protected AbstractArmorModel() {
    super(new ModelPart(List.of(), Map.of()), RenderTypes::entityCutout);
  }

  /** Sets up the model given the passed arguments */
  protected void setup(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> base) {
    this.base = base;
    this.hasGlint = stack.hasFoil();
    this.textureType = TextureType.fromSlot(slot);
    if (slot == EquipmentSlot.CHEST) {
      this.hasWings = ModifierUtil.checkVolatileFlag(stack, ModifiableArmorItem.ELYTRA);
      // DEFERRED RENDER: aligning the elytra wings used setupAnim(living, limbSwing, ...) + copyPropertiesTo, both replaced
      // by the 26.1 render-state pipeline (setupAnim(HumanoidRenderState) plus manual property copying); wing alignment is
      // validated in-game once re-hooked.
    } else {
      hasWings = false;
    }
  }

  /** Renders a colored model */
  public static void renderColored(Model<?> model, PoseStack matrices, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, int color, float red, float green, float blue, float alpha) {
    if (color != -1) {
      alpha *= (float)(color >> 24 & 255) / 255.0F;
      red *= (float)(color >> 16 & 255) / 255.0F;
      green *= (float)(color >> 8 & 255) / 255.0F;
      blue *= (float)(color & 255) / 255.0F;
    }
    model.renderToBuffer(matrices, buffer, packedLightIn, packedOverlayIn, ARGB.color((int)(alpha * 255.0f), (int)(red * 255.0f), (int)(green * 255.0f), (int)(blue * 255.0f)));
  }

  /** Renders the wings layer */
  protected void renderWings(PoseStack matrices, int packedLightIn, int packedOverlayIn, ArmorTexture texture, float red, float green, float blue, float alpha, boolean hasGlint) {
    matrices.pushPose();
    matrices.translate(0.0D, 0.0D, 0.125D);
    assert buffer != null;
    texture.renderTexture(getWings(), matrices, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha, hasGlint);
    matrices.popPose();
  }


  /* Helpers */

  /** Buffer from the render living event, stored as we lose access to it later */
  @Nullable
  public static MultiBufferSource buffer;

  /** Initializes the wrapper */
  public static void init() {
    // DEFERRED RENDER: the pre-26.1 buffer capture used RenderLivingEvent.Pre/Post#getMultiBufferSource, both removed in the
    // 26.1 render-state + SubmitNodeCollector rewrite. The custom armor layer draw that consumed the captured buffer is
    // deferred to the equipment-layer renderer, so no capture is registered here; validated in-game once re-hooked.
  }

  /** Wings model to render */
  @Nullable
  private static ElytraModel wingsModel;

  /** Gets or creates the elytra model */
  private ElytraModel getWings() {
    if (wingsModel == null) {
      wingsModel = new ElytraModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));
    }
    return wingsModel;
  }
}
