package modernmods.modernfoundry.library.tools.definition.module.material;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.ArmorType;
import modernmods.mantle.data.loadable.IAmLoadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.mapping.EitherLoadable;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.stats.IMaterialStats;
import modernmods.modernfoundry.library.materials.stats.IRepairableMaterialStats;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.ModifiableArmorMaterial;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.ArmorModuleBuilder;

import javax.annotation.Nullable;
import java.util.List;

/** Module for repairing a tool using a non-tool part material */
public class MaterialRepairModule implements MaterialRepairToolHook, ToolModule, IAmLoadable.Record {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaterialRepairModule>defaultHooks(ToolHooks.MATERIAL_REPAIR);
  private static final LoadableField<MaterialId,MaterialRepairModule> MATERIAL_FIELD = MaterialId.PARSER.requiredField("material", m -> m.material);
  private static final RecordLoadable<MaterialRepairModule> CONSTANT = RecordLoadable.create(MATERIAL_FIELD, IntLoadable.FROM_ONE.requiredField("durability", (MaterialRepairModule m) -> m.repairAmount), MaterialRepairModule::new);
  private static final RecordLoadable<StatType> STAT_TYPE = RecordLoadable.create(MATERIAL_FIELD, MaterialStatsId.PARSER.requiredField("stat_type", (StatType m) -> m.statType), MaterialRepairModule::of);
  public static final RecordLoadable<MaterialRepairModule> LOADER = EitherLoadable.<MaterialRepairModule>record().key("durability", CONSTANT).key("stat_type", STAT_TYPE).build(CONSTANT);

  /** Material used for repairing */
  protected final MaterialId material;
  /** Amount to repair */
  @Getter(AccessLevel.PROTECTED)
  protected int repairAmount;

  protected MaterialRepairModule(MaterialId material, int repairAmount) {
    this.material = material;
    this.repairAmount = repairAmount;
  }

  /** Creates a new module using a constant durability */
  public static MaterialRepairModule of(MaterialId material, int repairAmount) {
    return new MaterialRepairModule(material, repairAmount);
  }

  /** Creates a new module using a constant durability */
  public static MaterialRepairModule of(MaterialId material, ArmorType slot, float durabilityFactor) {
    return new MaterialRepairModule(material, (int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[slot.ordinal()] * durabilityFactor));
  }

  /** Creates a new module using a stat type lookup */
  public static MaterialRepairModule.StatType of(MaterialId material, MaterialStatsId statType) {
    return new MaterialRepairModule.StatType(material, statType);
  }

  /** Creates a builder for armor */
  public static ArmorBuilder armor(MaterialId material) {
    return new ArmorBuilder(material);
  }

  @Override
  public RecordLoadable<? extends MaterialRepairModule> loadable() {
    return CONSTANT;
  }

  @Override
  public boolean isRepairMaterial(IToolStackView tool, MaterialId material) {
    return this.material.equals(material);
  }

  @Override
  public float getRepairAmount(IToolStackView tool, MaterialId material) {
    return this.material.equals(material) ? repairAmount : 0;
  }

  @Override
  public RecordLoadable<MaterialRepairModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }


  /** Stat type implementation */
  private static class StatType extends MaterialRepairModule {
    /** Stat type used for repairing, null means it will be fetched as the first available stat type */
    private final MaterialStatsId statType;

    public StatType(MaterialId material, MaterialStatsId statType) {
      super(material, -1);
      this.statType = statType;
    }

    @Override
    public RecordLoadable<StatType> loadable() {
      return STAT_TYPE;
    }

    @Override
    public float getRepairAmount(IToolStackView tool, MaterialId material) {
      return this.material.equals(material) ? getRepairAmount(tool.getDefinition().getId()) : 0;
    }

    /** Gets and caches the repair amount for this module */
    private int getRepairAmount(@Nullable Identifier toolId) {
      if (repairAmount == -1) {
        repairAmount = getDurability(toolId, material, statType);
      }
      return repairAmount;
    }

    @Override
    protected int getRepairAmount() {
      return getRepairAmount(null);
    }
  }

  /** Gets the durability for the given stat type */
  public static int getDurability(@Nullable Identifier toolId, MaterialId material, MaterialStatsId statType) {
    IMaterialStats stats = MaterialRegistry.getInstance().getMaterialStats(material, statType).orElse(null);
    if (stats instanceof IRepairableMaterialStats repairable) {
      return repairable.durability();
    } else {
      if (toolId != null) {
        TConstruct.LOG.warn("Attempting to repair {} using {}, but stat type {}{}. This usually indicates a broken datapack.", toolId, material, statType, stats == null ? " does not exist for the material" : " does not contain durability");
      }
      return 0;
    }
  }


  /** Builder logic */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ArmorBuilder implements ArmorModuleBuilder<MaterialRepairModule> {
    private final MaterialId material;
    private final int[] durability = new int[4];

    /** Sets the durability for the piece based on the given factor */
    public ArmorBuilder durabilityFactor(float maxDamageFactor) {
      for (ArmorType slotType : ModifiableArmorMaterial.ARMOR_TYPES) {
        int index = slotType.ordinal();
        durability[index] = (int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[index] * maxDamageFactor);
      }
      return this;
    }

    @Override
    public MaterialRepairModule build(ArmorType slot) {
      return new MaterialRepairModule(material, durability[slot.ordinal()]);
    }
  }
}
