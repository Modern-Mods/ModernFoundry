package modernmods.modernfoundry.library.tools.definition.module.material;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.ArmorType;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.field.OptionallyNestedLoadable;
import modernmods.modernfoundry.library.materials.IMaterialRegistry;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.stats.IMaterialStats;
import modernmods.modernfoundry.library.materials.stats.MaterialStatType;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.definition.module.build.ToolStatsHook;
import modernmods.modernfoundry.library.tools.definition.module.build.ToolTraitHook;
import modernmods.modernfoundry.library.tools.helper.ModifierBuilder;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.MaterialNBT;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;
import modernmods.modernfoundry.tools.modules.ArmorModuleBuilder;
import modernmods.modernfoundry.tools.stats.PlatingMaterialStats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/** Module for building tool stats using materials */
public class MaterialStatsModule implements ToolStatsHook, ToolTraitHook, ToolMaterialHook, MaterialRepairToolHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaterialStatsModule>defaultHooks(ToolHooks.TOOL_STATS, ToolHooks.TOOL_TRAITS, ToolHooks.TOOL_MATERIALS, ToolHooks.MATERIAL_REPAIR);
  protected static final LoadableField<Integer,MaterialStatsModule> PRIMARY_PART_FIELD = IntLoadable.FROM_MINUS_ONE.defaultField("primary_part", 0, true, m -> m.primaryPart);
  public static final RecordLoadable<MaterialStatsModule> LOADER = RecordLoadable.create(
    new OptionallyNestedLoadable<>(MaterialStatsId.PARSER, "stat").list().requiredField("stat_types", m -> m.statTypes),
    new StatScaleField("stat", "stat_types"),
    PRIMARY_PART_FIELD,
    MaterialStatsModule::new);

  private final List<MaterialStatsId> statTypes;
  @Getter @VisibleForTesting
  final float[] scales;
  private int[] repairIndices;
  private final int primaryPart;

  protected MaterialStatsModule(List<MaterialStatsId> statTypes, float[] scales, int primaryPart) {
    this.statTypes = statTypes;
    this.scales = scales;
    this.primaryPart = primaryPart;
  }

  @Override
  public RecordLoadable<? extends MaterialStatsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addModules(ModuleHookMap.Builder builder) {
    // automatically add the primary part if not disabled
    if (primaryPart >= 0 && primaryPart < statTypes.size()) {
      builder.addHook(new MaterialTraitsModule(statTypes.get(primaryPart), primaryPart), ToolHooks.REBALANCED_TRAIT);
    }
  }

  @Override
  public List<MaterialStatsId> getStatTypes(ToolDefinition definition) {
    return statTypes;
  }

  @Override
  public float scaleStats(ToolDefinition definition, int index) {
    if (index < scales.length) {
      return scales[index];
    }
    return 1;
  }

  /** Gets the repair indices, calculating them if needed */
  private int[] getRepairIndices() {
    if (repairIndices == null) {
      IMaterialRegistry registry = MaterialRegistry.getInstance();
      repairIndices = IntStream.range(0, statTypes.size()).filter(i -> registry.canRepair(statTypes.get(i))).toArray();
    }
    return repairIndices;
  }

  @Override
  public boolean isRepairMaterial(IToolStackView tool, MaterialId material) {
    for (int part : getRepairIndices()) {
      if (tool.getMaterial(part).matches(material)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public float getRepairAmount(IToolStackView tool, MaterialId material) {
    // first, find all stat types we might try for this material
    Set<MaterialStatsId> matchingStats = new HashSet<>();
    for (int i : getRepairIndices()) {
      if (tool.getMaterial(i).matches(material)) {
        matchingStats.add(statTypes.get(i));
      }
    }
    // next, figure out which of the matches repairs the most
    Identifier toolId = tool.getDefinition().getId();
    int max = 0;
    for (MaterialStatsId stat : matchingStats) {
      // its possible a later stat type will repair more with this material
      int repair = MaterialRepairModule.getDurability(toolId, material, stat);
      if (repair > max) {
        max = repair;
      }
    }
    return max;
  }

  @Override
  public void addToolStats(IToolContext context, ModifierStatsBuilder builder) {
    MaterialNBT materials = context.getMaterials();
    if (!materials.isEmpty()) {
      IMaterialRegistry registry = MaterialRegistry.getInstance();
      for (int i = 0; i < statTypes.size(); i++) {
        MaterialStatsId statType = statTypes.get(i);
        // apply the stats for the material, assuming the stat ID is valid
        IMaterialStats stats = registry.getStatsOrDefault(materials.get(i).getId(), statType);
        if (stats != null) {
          stats.apply(builder, scales[i]);
        }
      }
    }
  }

  @Override
  public void addTraits(ToolDefinition definition, MaterialNBT materials, ModifierBuilder builder) {
    int max = Math.min(materials.size(), statTypes.size());
    if (max > 0) {
      IMaterialRegistry materialRegistry = MaterialRegistry.getInstance();
      for (int i = 0; i < max; i++) {
        builder.add(materialRegistry.getTraits(materials.get(i).getId(), statTypes.get(i)));
      }
    }
  }


  /* Builder */

  /** Creates a new builder instance */
  public static Builder stats() {
    return new Builder();
  }

  /** Starts a builder for armor stats */
  public static ArmorBuilder armorStats(List<ArmorType> slots) {
    return new ArmorBuilder(slots);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final ImmutableList.Builder<MaterialStatsId> stats = ImmutableList.builder();
    private final ImmutableList.Builder<Float> scales = ImmutableList.builder();
    @Setter @Accessors(fluent = true)
    private int primaryPart = 0;

    /** Adds a stat type */
    public Builder stat(MaterialStatsId stat, float scale) {
      stats.add(stat);
      scales.add(scale);
      return this;
    }

    /** Adds a stat type */
    public Builder stat(IMaterialStats stat, float scale) {
      return stat(stat.getIdentifier(), scale);
    }

    /** Adds a stat type */
    public Builder stat(MaterialStatType<?> stat, float scale) {
      return stat(stat.getStatId(), scale);
    }

    /** Adds a stat type */
    public Builder stat(MaterialStatsId stat) {
      return stat(stat, 1);
    }

    /** Adds a stat type */
    public Builder stat(IMaterialStats stat) {
      return stat(stat, 1);
    }

    /** Adds a stat type */
    public Builder stat(MaterialStatType<?> stat) {
      return stat(stat, 1);
    }

    /** Builds the array of scales from the list */
    static float[] buildScales(List<Float> list) {
      float[] scales = new float[list.size()];
      for (int i = 0; i < list.size(); i++) {
        scales[i] = list.get(i);
      }
      return scales;
    }

    /** Builds the module */
    public MaterialStatsModule build() {
      List<MaterialStatsId> stats = this.stats.build();
      if (primaryPart >= stats.size() || primaryPart < -1) {
        throw new IllegalStateException("Primary part must be within parts list, maximum " + stats.size() + ", got " + primaryPart);
      }
      return new MaterialStatsModule(stats, buildScales(scales.build()), primaryPart);
    }
  }

  /** Builder for armor */
  public static class ArmorBuilder implements ArmorModuleBuilder<MaterialStatsModule> {
    private final List<ArmorType> slotTypes;
    private final Builder[] builders = new Builder[4];

    private ArmorBuilder(List<ArmorType> slotTypes) {
      this.slotTypes = slotTypes;
      for (ArmorType slotType : slotTypes) {
        builders[slotType.ordinal()] = new MaterialStatsModule.Builder();
      }
    }

    /** Gets the builder for the given slot */
    protected Builder getBuilder(ArmorType slotType) {
      Builder builder = builders[slotType.ordinal()];
      if (builder == null) {
        throw new IllegalArgumentException("Unsupported slot type " + slotType);
      }
      return builder;
    }

    /** Adds a stat to the given slot */
    public ArmorBuilder part(ArmorType slotType, MaterialStatsId stat, float scale) {
      getBuilder(slotType).stat(stat, scale);
      return this;
    }

    /** Adds a stat to all slots */
    public ArmorBuilder stat(MaterialStatsId stat, float scale) {
      for (ArmorType slotType : slotTypes) {
        getBuilder(slotType).stat(stat, scale);
      }
      return this;
    }

    /** Adds a stat to all slots */
    public ArmorBuilder stat(IMaterialStats stat, float scale) {
      return stat(stat.getIdentifier(), scale);
    }

    /** Adds a stat to all slots from the given stat type list */
    public ArmorBuilder stat(List<? extends MaterialStatType<?>> stats, float scale) {
      for (ArmorType slotType : slotTypes) {
        getBuilder(slotType).stat(stats.get(slotType.ordinal()).getStatId(), scale);
      }
      return this;
    }

    /** Adds a plating part type */
    public ArmorBuilder plating(float scale) {
      return stat(PlatingMaterialStats.TYPES, scale);
    }

    /** Sets the primary part for all slots, assuming its the same index as you defined the parts using this builder. */
    public ArmorBuilder primaryPart(int index) {
      for (ArmorType slotType : slotTypes) {
        getBuilder(slotType).primaryPart(index);
      }
      return this;
    }

    @Override
    public MaterialStatsModule build(ArmorType slot) {
      return getBuilder(slot).build();
    }
  }
}
