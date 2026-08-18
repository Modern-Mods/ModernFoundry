package modernmods.modernfoundry.library.client.model.tools;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4fc;
import modernmods.mantle.client.model.util.DynamicItemModel;
import modernmods.mantle.client.model.util.MantleItemLayerModel;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.client.modifiers.IBakedModifierModel;
import modernmods.modernfoundry.library.client.modifiers.ModifierModelMap;
import modernmods.modernfoundry.library.client.modifiers.ModifierModelMapManager;
import modernmods.modernfoundry.library.client.modifiers.model.ModifierModel;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.worktable.ModifierSetWorktableRecipe;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.MaterialIdNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Item model handling all tools, both multipart and non.
 * <p>
 * In 26.1 the removed {@code IUnbakedGeometry}/{@code ItemOverrides}/{@code BakedModel} pipeline is replaced by the item
 * model system: this is an {@link ItemModel.Unbaked} baking to a {@link DynamicItemModel} keyed on a {@link ToolCacheKey}
 * (the tool's material list plus the modifier cache keys). Material tints and emissivity are baked directly into the quads
 * (via {@link MaterialModel#getQuadsForMaterial}), replacing the removed item-color handler.
 * <p>
 * IN-GAME VALIDATION: the following visual details of the pre-26.1 model bake only make sense on the immutable
 * {@link QuadCollection} pipeline once rendered in-game and are structured here but not yet reproduced exactly:
 * <ul>
 *   <li>Per-context model swapping (large in-hand 2x stretch vs. small vs. GUI front-face vs. left-hand). A single
 *       identity-transformed geometry is baked, which is correct for the GUI/inventory and non-large tools; the large
 *       in-hand stretch and left-hand ammo shift need a per-{@link ItemDisplayContext} model set.</li>
 *   <li>The {@link ItemLayerPixels} per-material occlusion mask ordering.</li>
 *   <li>Dynamic ammo overlay quads (bows/crossbows): the removed {@code ItemRenderer#getModel}/{@code BakedModel#getQuads}
 *       extraction is not reproduced; ammo config is retained for a future rebuild.</li>
 * </ul>
 */
public final class ToolModel {
  private ToolModel() {}

  /** Registered id for this item model type */
  public static final Identifier ID = TConstruct.getResource("tool");

  /** Set of transform types that make tools render small */
  private static final BitSet SMALL_TOOL_TYPES = new BitSet();

  /**
   * Registers a new small tool transform type. Retained so callers keep registering small contexts; per-context model
   * selection is validated in-game (see class docs).
   */
  public static synchronized void registerSmallTool(ItemDisplayContext type) {
    SMALL_TOOL_TYPES.set(type.ordinal());
  }

  /** Codec for an offset pair, in pixels */
  private static final Codec<Vec2> OFFSET_CODEC = Codec.FLOAT.listOf().comapFlatMap(
    list -> list.size() == 2 ? DataResult.success(new Vec2(list.get(0), list.get(1))) : DataResult.error(() -> "Offset must have 2 values"),
    vec -> List.of(vec.x, vec.y));


  /* Data records */

  /** Data class for a single tool part */
  private record ToolPart(String name, int index) {
    public static final Codec<ToolPart> CODEC = RecordCodecBuilder.create(inst -> inst.group(
      Codec.STRING.fieldOf("name").forGetter(ToolPart::name),
      Codec.INT.optionalFieldOf("index", -1).forGetter(ToolPart::index)
    ).apply(inst, ToolPart::new));
    /** Default tool part instance for breakable textures */
    public static final ToolPart DEFAULT = new ToolPart("tool", -1);
    /** Default tool part list if one is not defined */
    public static final List<ToolPart> DEFAULT_PARTS = List.of(DEFAULT);

    /** If true, this part has material variants */
    public boolean hasMaterials() {
      return index >= 0;
    }

    /** Gets the name for this part */
    public String getName(boolean isLarge) {
      if (isLarge) {
        return "large_" + name;
      }
      return name;
    }
  }

  /** Codec for a modifier id */
  private static final Codec<ModifierId> MODIFIER_ID_CODEC = Identifier.CODEC.xmap(ModifierId::new, ModifierId::getIdentifier);

  /** Modifier that may be forced */
  private record FirstModifier(ModifierId id, boolean forced) {
    public static final Codec<FirstModifier> CODEC = Codec.either(
      MODIFIER_ID_CODEC,
      RecordCodecBuilder.<FirstModifier>create(inst -> inst.group(
        MODIFIER_ID_CODEC.fieldOf("name").forGetter(FirstModifier::id),
        Codec.BOOL.optionalFieldOf("forced", false).forGetter(FirstModifier::forced)
      ).apply(inst, FirstModifier::new))
    ).xmap(
      either -> either.map(id -> new FirstModifier(id, false), Function.identity()),
      first -> first.forced ? com.mojang.datafixers.util.Either.right(first) : com.mojang.datafixers.util.Either.left(first.id));

    /** Gets the index of a modifier in the list */
    public static int indexOf(List<FirstModifier> list, ModifierId id) {
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).id.equals(id)) {
          return i;
        }
      }
      return -1;
    }
  }

  /** Ammo display config; retained from JSON for a future dynamic-ammo rebuild (see class docs) */
  private record AmmoConfig(Identifier key, boolean flip, boolean left, Vec2 smallOffset, Vec2 largeOffset) {
    public static final Codec<AmmoConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
      Identifier.CODEC.fieldOf("key").forGetter(AmmoConfig::key),
      Codec.BOOL.optionalFieldOf("flip", false).forGetter(AmmoConfig::flip),
      Codec.BOOL.optionalFieldOf("left", false).forGetter(AmmoConfig::left),
      OFFSET_CODEC.optionalFieldOf("small_offset", Vec2.ZERO).forGetter(AmmoConfig::smallOffset),
      OFFSET_CODEC.optionalFieldOf("large_offset", Vec2.ZERO).forGetter(AmmoConfig::largeOffset)
    ).apply(inst, AmmoConfig::new));
  }

  /** Cache key holding everything unique to a baked tool model. Identity considers only the materials and modifier data. */
  static final class ToolCacheKey {
    private final List<MaterialVariantId> materials;
    private final List<Object> modifierData;
    /** Tool carried for baking modifier quads; excluded from identity */
    @Nullable
    private final IToolStackView tool;

    ToolCacheKey(List<MaterialVariantId> materials, List<Object> modifierData, @Nullable IToolStackView tool) {
      this.materials = materials;
      this.modifierData = modifierData;
      this.tool = tool;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ToolCacheKey other)) {
        return false;
      }
      return materials.equals(other.materials) && modifierData.equals(other.modifierData);
    }

    @Override
    public int hashCode() {
      return Objects.hash(materials, modifierData);
    }
  }


  /* Modifier quads */

  /**
   * Adds quads for relevant modifiers
   * @param spriteGetter    Sprite getter instance
   * @param modifierModels  Map of modifier models
   * @param tool            Tool instance
   * @param quadConsumer    Consumer for finished quads
   * @param transforms      Transforms to apply
   * @param isLarge         If true, the quads are for a large tool
   */
  private static void addModifierQuads(Function<Material, TextureAtlasSprite> spriteGetter, ModifierModelMap modifierModels, List<FirstModifier> firstModifiers, boolean showTraits, IToolStackView tool, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels, Transformation transforms, boolean isLarge) {
    if (!modifierModels.isEmpty()) {
      // keep a running tint index so models know where they should start, currently starts at 0 as the main model does not use tint indexes
      int modelIndex = 0;
      // reversed order to ensure the pixels is updated correctly
      List<ModifierEntry> modifiers = (showTraits ? tool.getModifiers() : tool.getUpgrades()).getModifiers();
      // keep track of the entry for each first modifier, as that may impact how it renders
      ModifierEntry[] firsts = new ModifierEntry[firstModifiers.size()];
      if (!modifiers.isEmpty()) {
        // last, add all regular modifiers
        Set<ModifierId> hidden = ModifierSetWorktableRecipe.getModifierSet(tool.getPersistentData(), TConstruct.getResource("invisible_modifiers"));
        for (int i = modifiers.size() - 1; i >= 0; i--) {
          ModifierEntry entry = modifiers.get(i);
          ModifierId modifier = entry.getModifier().getId();
          int index = FirstModifier.indexOf(firstModifiers, modifier);
          if (index != -1) {
            // handle first modifiers later
            firsts[index] = entry;
          } else if (!hidden.contains(modifier)) {
            IBakedModifierModel model = modifierModels.get(modifier);
            if (model != null) {
              // if the modifier is in the list, delay adding its quads, but keep the expected tint index
              model.addQuads(tool, entry, spriteGetter, transforms, isLarge, modelIndex, quadConsumer, pixels);
              modelIndex += model.getTintIndexes();
            }
          }
        }
      }
      // first, add the first modifiers
      for (int i = firsts.length - 1; i >= 0; i--) {
        ModifierEntry entry = firsts[i];
        FirstModifier first = firstModifiers.get(i);
        if (entry != null || first.forced) {
          IBakedModifierModel model = modifierModels.get(first.id);
          if (model != null) {
            if (entry == null) {
              entry = new ModifierEntry(first.id, 0);
            }
            model.addQuads(tool, entry, spriteGetter, transforms, isLarge, modelIndex, quadConsumer, pixels);
            modelIndex += model.getTintIndexes();
          }
        }
      }
      // iterate the constant models in order for simplicity
      for (ModifierModel model : modifierModels.constant().values()) {
        model.addQuads(tool, ModifierEntry.EMPTY, spriteGetter, transforms, isLarge, modelIndex, quadConsumer, pixels);
        modelIndex += model.getTintIndexes();
      }
    }
  }


  /* Item model */

  /**
   * Adds an item-layer quad. The extruded side walls (X/Z-plane faces, i.e. non-Z-axis normals) are emitted from both
   * winding directions so the item shows thickness from any angle. The front/back fill faces (Z-axis normals) are left
   * single-sided: a coplanar reverse-wound copy sits at the same z as the original and, drawn later, wins the equal-z
   * depth test; facing away from the light it renders the fill near-black, leaving the tool looking like a hollow shell
   * with no interior fill. Keeping the fill single-sided draws it lit and solid.
   */
  private static void addToolQuad(QuadCollection.Builder builder, BakedQuad quad) {
    builder.addUnculledFace(quad);
    if (quad.direction().getAxis() != Direction.Axis.Z) {
      builder.addUnculledFace(new BakedQuad(
        quad.position0(), quad.position3(), quad.position2(), quad.position1(),
        quad.packedUV0(), quad.packedUV3(), quad.packedUV2(), quad.packedUV1(),
        quad.direction().getOpposite(), quad.materialInfo()));
    }
  }

  /**
   * Unbaked tool item model.
   * @param baseModel          Model providing the texture slots (part textures) and transforms
   * @param parts              List of tool parts in this model
   * @param isLarge            If true, this is a large tool and uses double resolution textures in hand
   * @param largeOffset        Pixel offset applied to the large model
   * @param modifierModels     List of modifier model maps to read
   * @param smallModifierRoots Legacy small modifier texture roots
   * @param largeModifierRoots Legacy large modifier texture roots
   * @param firstModifiers     Modifiers shown first on the tool
   * @param ammo               Optional ammo display config
   * @param showTraits         If true, traits are displayed on the tool model
   */
  /**
   * Small/large modifier texture roots. Authored as either a flat array (non-large tools, where the same roots serve
   * every context) or an object {@code {"small":[...],"large":[...]}} (large tools, whose in-hand render uses a distinct
   * double-resolution root). The single {@code small_modifier_roots} JSON key carries both forms, matching the authored
   * item models; a bare array maps small == large.
   */
  private record ModifierRoots(List<Identifier> small, List<Identifier> large) {
    static final ModifierRoots EMPTY = new ModifierRoots(List.of(), List.of());
    private static final Codec<List<Identifier>> LIST = Identifier.CODEC.listOf();
    private static final Codec<ModifierRoots> OBJECT = RecordCodecBuilder.create(inst -> inst.group(
      LIST.optionalFieldOf("small", List.of()).forGetter(ModifierRoots::small),
      LIST.optionalFieldOf("large", List.of()).forGetter(ModifierRoots::large)
    ).apply(inst, ModifierRoots::new));
    static final Codec<ModifierRoots> CODEC = Codec.either(LIST, OBJECT).xmap(
      either -> either.map(list -> new ModifierRoots(list, list), Function.identity()),
      roots -> roots.small().equals(roots.large()) ? Either.left(roots.small()) : Either.right(roots));
  }

  public record Unbaked(Identifier baseModel, List<ToolPart> parts, boolean isLarge, Vec2 largeOffset,
                        List<Identifier> modifierModels, ModifierRoots modifierRoots,
                        List<FirstModifier> firstModifiers, Optional<AmmoConfig> ammo, boolean showTraits) implements ItemModel.Unbaked {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Identifier.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
      ToolPart.CODEC.listOf().optionalFieldOf("parts", List.of()).forGetter(Unbaked::parts),
      Codec.BOOL.optionalFieldOf("large", false).forGetter(Unbaked::isLarge),
      OFFSET_CODEC.optionalFieldOf("large_offset", Vec2.ZERO).forGetter(Unbaked::largeOffset),
      Identifier.CODEC.listOf().optionalFieldOf("modifier_maps", List.of()).forGetter(Unbaked::modifierModels),
      ModifierRoots.CODEC.optionalFieldOf("small_modifier_roots", ModifierRoots.EMPTY).forGetter(Unbaked::modifierRoots),
      FirstModifier.CODEC.listOf().optionalFieldOf("first_modifiers", List.of()).forGetter(Unbaked::firstModifiers),
      AmmoConfig.CODEC.optionalFieldOf("ammo").forGetter(Unbaked::ammo),
      Codec.BOOL.optionalFieldOf("show_traits", false).forGetter(Unbaked::showTraits)
    ).apply(inst, Unbaked::new));

    /** Small-context modifier texture roots (see {@link ModifierRoots}). */
    public List<Identifier> smallModifierRoots() {
      return modifierRoots.small();
    }

    /** Large-context (in-hand double-resolution) modifier texture roots (see {@link ModifierRoots}). */
    public List<Identifier> largeModifierRoots() {
      return modifierRoots.large();
    }

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
      List<ToolPart> toolParts = parts.isEmpty() ? ToolPart.DEFAULT_PARTS : parts;
      return new Baked(context, transformation, baseModel, toolParts, isLarge, modifierModels, smallModifierRoots(), largeModifierRoots(), firstModifiers, showTraits);
    }
  }

  /** Baked tool item model that resolves the geometry per tool NBT. */
  private static final class Baked extends DynamicItemModel<ToolCacheKey> {
    private final Identifier baseModelId;
    private final List<ToolPart> toolParts;
    private final boolean isLarge;
    private final List<Identifier> modifierModels;
    private final List<Identifier> smallModifierRoots;
    private final List<Identifier> largeModifierRoots;
    private final List<FirstModifier> firstModifiers;
    private final boolean showTraits;

    // lazily resolved baking data, stable for the captured context
    @Nullable
    private ResolvedModel resolved;
    @Nullable
    private TextureSlots slots;
    @Nullable
    private Function<Material, TextureAtlasSprite> spriteGetter;
    @Nullable
    private ModifierModelMap modifierModelMap;
    @Nullable
    private ItemModel fallbackModel;

    private Baked(ItemModel.BakingContext context, Matrix4fc transform, Identifier baseModelId, List<ToolPart> toolParts, boolean isLarge,
                 List<Identifier> modifierModels, List<Identifier> smallModifierRoots, List<Identifier> largeModifierRoots,
                 List<FirstModifier> firstModifiers, boolean showTraits) {
      super(context, transform);
      this.baseModelId = baseModelId;
      this.toolParts = toolParts;
      this.isLarge = isLarge;
      this.modifierModels = modifierModels;
      this.smallModifierRoots = smallModifierRoots;
      this.largeModifierRoots = largeModifierRoots;
      this.firstModifiers = firstModifiers;
      this.showTraits = showTraits;
    }

    /** Resolves the baker-dependent data once for the captured baking context */
    private void ensureResolved() {
      if (resolved == null) {
        ModelBaker baker = context.blockModelBaker();
        resolved = baker.getModel(baseModelId);
        slots = resolved.getTopTextureSlots();
        ResolvedModel resolvedModel = resolved;
        spriteGetter = mat -> baker.materials().get(mat, resolvedModel).sprite();
        modifierModelMap = ModifierModelMapManager.INSTANCE.getModelsForTool(spriteGetter, modifierModels, smallModifierRoots, largeModifierRoots, baseModelId);
      }
    }

    @Nullable
    @Override
    protected ToolCacheKey getCacheKey(ItemStack stack) {
      List<MaterialVariantId> materialIds = MaterialIdNBT.from(stack).getMaterials();
      IToolStackView tool = ToolStack.from(stack);

      // if nothing unique, render the fallback (base model without materials/modifiers)
      ModifierNBT modifiers = showTraits ? tool.getModifiers() : tool.getUpgrades();
      boolean forced = false;
      for (FirstModifier modifier : firstModifiers) {
        if (modifier.forced) {
          forced = true;
          break;
        }
      }
      if (materialIds.isEmpty() && modifiers.isEmpty() && !forced) {
        return null;
      }

      // ensure modifier models are loaded so we can compute their cache keys
      ensureResolved();
      ModifierModelMap models = Objects.requireNonNull(modifierModelMap);

      // build the modifier cache key list, based on what each modifier requests
      ImmutableList.Builder<Object> builder = ImmutableList.builder();
      Set<ModifierId> hidden = ModifierSetWorktableRecipe.getModifierSet(tool.getPersistentData(), TConstruct.getResource("invisible_modifiers"));
      ModifierEntry[] firstEntries = new ModifierEntry[firstModifiers.size()];
      for (ModifierEntry entry : modifiers.getModifiers()) {
        ModifierId id = entry.getId();
        int index = FirstModifier.indexOf(firstModifiers, id);
        if (index != -1) {
          firstEntries[index] = entry;
        } else if (!hidden.contains(id)) {
          IBakedModifierModel model = models.get(id);
          if (model != null) {
            Object cacheKey = model.getCacheKey(tool, entry);
            if (cacheKey != null) {
              builder.add(cacheKey);
            }
          }
        }
      }
      for (int i = 0; i < firstModifiers.size(); i++) {
        FirstModifier modifier = firstModifiers.get(i);
        ModifierEntry entry = firstEntries[i];
        if (entry != null || modifier.forced) {
          IBakedModifierModel model = models.get(modifier.id);
          if (model != null) {
            if (entry == null) {
              entry = new ModifierEntry(modifier.id, 0);
            }
            Object cacheKey = model.getCacheKey(tool, entry);
            if (cacheKey != null) {
              builder.add(cacheKey);
            }
          }
        }
      }
      for (ModifierModel model : models.constant().values()) {
        Object cacheKey = model.getCacheKey(tool, ModifierEntry.EMPTY);
        if (cacheKey != null) {
          builder.add(cacheKey);
        }
      }

      return new ToolCacheKey(materialIds, builder.build(), tool);
    }

    @Override
    protected ItemModel getFallback() {
      if (fallbackModel == null) {
        fallbackModel = bake(List.of(), null);
      }
      return fallbackModel;
    }

    @Override
    protected ItemModel bakeModel(ToolCacheKey key) {
      return bake(key.materials, key.tool);
    }

    /**
     * Bakes the tool geometry for the given materials and tool.
     * <p>
     * A single identity-transformed {@link QuadCollection} is produced (correct for GUI/inventory and non-large tools);
     * per-context large/small/left-hand variants and ammo overlays are validated in-game (see class docs).
     */
    private ItemModel bake(List<MaterialVariantId> materials, @Nullable IToolStackView tool) {
      ensureResolved();
      ModelBaker baker = context.blockModelBaker();
      ResolvedModel resolvedModel = Objects.requireNonNull(resolved);
      TextureSlots textureSlots = Objects.requireNonNull(slots);
      Function<Material, TextureAtlasSprite> sprites = Objects.requireNonNull(spriteGetter);
      ModifierModelMap models = Objects.requireNonNull(modifierModelMap);

      QuadCollection.Builder builder = new QuadCollection.Builder();
      ItemLayerPixels pixels = new ItemLayerPixels();
      Transformation transforms = Transformation.IDENTITY;

      // add modifier quads first so the pixel mask is populated before the parts beneath
      if (tool != null && !models.isEmpty()) {
        addModifierQuads(sprites, models, firstModifiers, showTraits, tool, quads -> quads.forEach(quad -> addToolQuad(builder, quad)), pixels, transforms, false);
      }

      // add quads for all parts, in reverse so earlier parts render on top
      for (int i = toolParts.size() - 1; i >= 0; i--) {
        ToolPart part = toolParts.get(i);
        String name = part.getName(false);
        Material texture = textureSlots.getMaterial(name);
        if (texture == null) {
          continue;
        }
        if (part.hasMaterials()) {
          int index = part.index();
          MaterialVariantId material = index < materials.size() ? materials.get(index) : IMaterial.UNKNOWN_ID;
          if (Config.CLIENT.logMissingMaterialTextures.get()) {
            MaterialModel.validateMaterialTextures(textureSlots, sprites, name, material);
          }
          for (BakedQuad quad : MaterialModel.getQuadsForMaterial(sprites, texture, material, -1, transforms, pixels)) {
            addToolQuad(builder, quad);
          }
        } else {
          Material.Baked sprite = baker.materials().resolveSlot(textureSlots, name, resolvedModel);
          for (BakedQuad quad : MantleItemLayerModel.getQuadsForSprite(-1, -1, sprite, transforms, 0, pixels)) {
            addToolQuad(builder, quad);
          }
        }
      }

      QuadCollection quads = builder.build();
      ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
      return new CuboidItemModelWrapper(List.of(), quads, properties, transform);
    }
  }
}
