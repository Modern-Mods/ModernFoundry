package modernmods.modernfoundry.library.client.materials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import modernmods.mantle.data.gson.ResourceLocationSerializer;
import modernmods.mantle.data.loadable.common.GsonLoadable;
import modernmods.mantle.data.loadable.field.LegacyField;
import modernmods.mantle.data.loadable.primitive.BooleanLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.data.spritetransformer.IColorMapping;
import modernmods.modernfoundry.library.client.data.spritetransformer.ISpriteTransformer;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;

import java.util.Set;

/**
 * Component of {@link MaterialRenderInfo} used during datagen and the generate part textures command to describe how to generate the material
 */
@RequiredArgsConstructor
public class MaterialGeneratorInfo {
  /** GSON adapter for generator deserializing. TODO: migrate ISpriteTransformer to loadables? */
  private static final Gson GSON = (new GsonBuilder())
    .registerTypeAdapter(Identifier.class, ResourceLocationSerializer.resourceLocation(TConstruct.MOD_ID))
    .registerTypeAdapter(MaterialStatsId.class, (com.google.gson.JsonDeserializer<MaterialStatsId>) (element, type, ctx) -> {
      String loc = net.minecraft.util.GsonHelper.convertToString(element, "location");
      if (!loc.contains(":")) { loc = TConstruct.MOD_ID + ":" + loc; }
      return new MaterialStatsId(loc);
    })
    .registerTypeHierarchyAdapter(ISpriteTransformer.class, ISpriteTransformer.SERIALIZER)
    .registerTypeHierarchyAdapter(IColorMapping.class, IColorMapping.SERIALIZER)
    .create();
  public static final RecordLoadable<MaterialGeneratorInfo> LOADABLE = RecordLoadable.create(
    new GsonLoadable<>(GSON, ISpriteTransformer.class).requiredField("transformer", g -> g.transformer),
    new LegacyField<>(MaterialStatsId.PARSER.set(0).requiredField("supported_stats", g -> g.supportedStats), "supportedStats"),
    new LegacyField<>(BooleanLoadable.INSTANCE.defaultField("ignore_material_stats", false, false, g -> g.ignoreMaterialStats), "ignoreMaterialStats"),
    BooleanLoadable.INSTANCE.defaultField("variant", false, false, g -> g.variant),
    MaterialGeneratorInfo::new);

  /** Transformer to update images */
  @Getter
  private final ISpriteTransformer transformer;
  /** List of stat types supported by this material */
  private final Set<MaterialStatsId> supportedStats;
  /** If true, this ignores the material stats when determining applicable stat types for the command. Only affects the command, not datagen */
  protected final boolean ignoreMaterialStats;
  /** If true, this tool will not generate variant textures. Used on ancient tools to skip textures that will never appear. */
  @Getter
  private final boolean variant;

  public MaterialGeneratorInfo(MaterialGeneratorInfo other) {
    this(other.transformer, other.supportedStats, other.ignoreMaterialStats, other.variant);
  }

  /** If true, this stat type is supported */
  public boolean supportStatType(MaterialStatsId statType) {
    return supportedStats.contains(statType);
  }
}
