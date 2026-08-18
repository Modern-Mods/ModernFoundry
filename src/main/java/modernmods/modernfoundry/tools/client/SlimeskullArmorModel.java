package modernmods.modernfoundry.tools.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.data.listener.ISafeManagerReloadListener;
import modernmods.modernfoundry.library.client.armor.ArmorModelManager.ArmorModel;
import modernmods.modernfoundry.library.client.armor.MultilayerArmorModel;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfo;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfoLoader;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.tools.nbt.MaterialIdNBT;
import modernmods.modernfoundry.library.utils.SimpleCache;
import modernmods.modernfoundry.world.client.BlockModelSkullRenderer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Model to render a slimeskull helmet with both the helmet and skull.
 * Minimal placeholder shape pending the armor Model render-system rewrite (Model#renderToBuffer is now final); the combined helmet/skull submission is deferred while head model registration and setup state are retained.
 */
public class SlimeskullArmorModel extends MultilayerArmorModel {
  /** Singleton model instance, all data is passed in via setters */
  public static final SlimeskullArmorModel INSTANCE = new SlimeskullArmorModel();
  /** Cache of colors for materials */
  private static final SimpleCache<MaterialVariantId,Integer> MATERIAL_COLOR_CACHE = new SimpleCache<>(mat ->
    MaterialRenderInfoLoader.INSTANCE.getRenderInfo(mat)
            .map(MaterialRenderInfo::vertexColor)
            .orElse(-1));
  /** Listener to clear caches */
  public static final ISafeManagerReloadListener RELOAD_LISTENER = manager -> {
    HEAD_MODELS = null;
    MATERIAL_COLOR_CACHE.clear();
  };

  /** Head to render under the helmet */
  @Nullable
  private Identifier headTexture;
  /** Tint color for the head */
  private int headColor = -1;
  /** Texture for the head */
  @Nullable
  private SkullModelBase headModel;
  /** Current animation time for the skull */
  private float walkAnimation = 0;

  private SlimeskullArmorModel() {}

  /** Prepares the model */
  public Model setup(LivingEntity living, ItemStack stack, HumanoidModel<?> base, ArmorModel model) {
    super.setup(living, stack, EquipmentSlot.HEAD, base, model);
    MaterialId materialId = MaterialIdNBT.from(stack).getMaterial(0).getId();
    if (!materialId.equals(IMaterial.UNKNOWN_ID)) {
      SkullModelBase skull = getHeadModel(materialId);
      Identifier texture = HEAD_TEXTURES.get(materialId);
      if (skull != null && texture != null) {
        headModel = skull;
        headTexture = texture;
        // determine the color to tint the helmet, fallback to no tint if missing
        MaterialVariantId material = MaterialIdNBT.from(stack).getMaterial(1);
        if (IMaterial.UNKNOWN_ID.equals(material)) {
          headColor = -1;
        } else {
          headColor = MATERIAL_COLOR_CACHE.apply(material);
        }

        // setup walk animation
        WalkAnimationState walkState = living.getVehicle() instanceof LivingEntity vehicle ? vehicle.walkAnimation : living.walkAnimation;
        this.walkAnimation = walkState.position();
        return this;
      }
    }
    headTexture = null;
    headModel = null;
    headColor = -1;
    walkAnimation = 0;
    return this;
  }


  /* Head models */

  /** Map of all skull factories */
  private static final Map<MaterialId,Function<EntityModelSet,? extends SkullModelBase>> HEAD_MODEL_FACTORIES = new HashMap<>();
  /** Map of texture for the skull textures */
  private static final Map<MaterialId,Identifier> HEAD_TEXTURES = new HashMap<>();

  /** Registers a head model and texture, using the default skull model */
  public static void registerHeadModel(MaterialId materialId, ModelLayerLocation headModel, Identifier texture) {
    registerHeadModel(materialId, modelSet -> new SkullModel(modelSet.bakeLayer(headModel)), texture);
  }

  /** Registers a head model and texture, using the piglin skull model */
  public static void registerPiglinHeadModel(MaterialId materialId, ModelLayerLocation headModel, Identifier texture) {
    registerHeadModel(materialId, modelSet -> new PiglinHeadModel(modelSet.bakeLayer(headModel)), texture);
  }

  /** Registers a skull model using an item as the model */
  public static void registerBlockModel(MaterialId materialId, ItemStack stack) {
    registerHeadModel(materialId, modelSet -> new BlockModelSkullRenderer(Minecraft.getInstance().getItemModelResolver(), stack), net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
  }

  /** Registers a head model and texture, using a custom skull model */
  public static void registerHeadModel(MaterialId materialId, Function<EntityModelSet,? extends SkullModelBase> headFunction, Identifier texture) {
    if (HEAD_MODEL_FACTORIES.containsKey(materialId)) {
      throw new IllegalArgumentException("Duplicate head model " + materialId);
    }
    HEAD_MODEL_FACTORIES.put(materialId, headFunction);
    HEAD_TEXTURES.put(materialId, texture);
  }

  /** Map of baked head models, if null it is not currently computed */
  private static Map<MaterialId, SkullModelBase> HEAD_MODELS;

  /** Gets the head model for the given material */
  @Nullable
  private static SkullModelBase getHeadModel(MaterialId materialId) {
    if (HEAD_MODELS == null) {
      // vanilla rebakes these a lot, so figure we should at least do it every resource reload
      EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
      ImmutableMap.Builder<MaterialId,SkullModelBase> models = ImmutableMap.builder();
      for (Entry<MaterialId,Function<EntityModelSet,? extends SkullModelBase>> entry : HEAD_MODEL_FACTORIES.entrySet()) {
        models.put(entry.getKey(), entry.getValue().apply(modelSet));
      }
      HEAD_MODELS = models.build();
    }
    return HEAD_MODELS.get(materialId);
  }
}
