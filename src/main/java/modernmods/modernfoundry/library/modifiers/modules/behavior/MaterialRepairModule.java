package modernmods.modernfoundry.library.modifiers.modules.behavior;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import modernmods.mantle.data.loadable.IAmLoadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.mapping.EitherLoadable;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.MaterialRepairModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModuleBuilder;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

import static modernmods.modernfoundry.library.tools.definition.module.material.MaterialRepairModule.getDurability;

/** Collection of modules for different repair options */
public sealed class MaterialRepairModule implements ModifierModule, MaterialRepairModifierHook, ConditionalModule<IToolStackView>, IAmLoadable.Record permits MaterialRepairModule.StatType {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaterialRepairModule>defaultHooks(ModifierHooks.MATERIAL_REPAIR);
  private static final LoadableField<MaterialId, MaterialRepairModule> MATERIAL_FIELD = MaterialId.PARSER.requiredField("material", m -> m.material);
  private static final RecordLoadable<MaterialRepairModule> CONSTANT = RecordLoadable.create(MATERIAL_FIELD, IntLoadable.FROM_ONE.requiredField("durability", (MaterialRepairModule m) -> m.repairAmount), ModifierCondition.TOOL_FIELD, MaterialRepairModule::new);
  private static final RecordLoadable<StatType> STAT_TYPE = RecordLoadable.create(MATERIAL_FIELD, MaterialStatsId.PARSER.requiredField("stat_type", (StatType m) -> m.statType), ModifierCondition.TOOL_FIELD, StatType::new);
  public static final RecordLoadable<MaterialRepairModule> LOADER = EitherLoadable.<MaterialRepairModule>record().key("durability", CONSTANT).key("stat_type", STAT_TYPE).build(CONSTANT);

  /** Material used for repairing */
  protected final MaterialId material;
  /** Amount to repair */
  protected int repairAmount;
  /** Conditions to apply this module */
  private final ModifierCondition<IToolStackView> condition;

  protected MaterialRepairModule(MaterialId material, int repairAmount, ModifierCondition<IToolStackView> condition) {
    this.material = material;
    this.repairAmount = repairAmount;
    this.condition = condition;
  }

  @Override
  public ModifierCondition<IToolStackView> condition() {
    return condition;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<? extends MaterialRepairModule> getLoader() {
    return LOADER;
  }

  @Override
  public RecordLoadable<? extends Record> loadable() {
    return CONSTANT;
  }

  @Override
  public boolean isRepairMaterial(IToolStackView tool, ModifierEntry modifier, MaterialId material) {
    return this.material.equals(material) && condition().matches(tool, modifier);
  }

  /** Gets the repair amount for the given tool after validating conditions */
  protected int getRepairAmount(IToolStackView tool) {
    return repairAmount;
  }

  @Override
  public float getRepairAmount(IToolStackView tool, ModifierEntry modifier, MaterialId material) {
    return isRepairMaterial(tool, modifier, material) ? getRepairAmount(tool) * modifier.getLevel(): 0;
  }

  /** Repair method that does a lookup for a stat type to determine repair amount */
  static final class StatType extends MaterialRepairModule {
    private final MaterialStatsId statType;
    public StatType(MaterialId material, MaterialStatsId statType, ModifierCondition<IToolStackView> condition) {
      super(material, -1, condition);
      this.statType = statType;
    }

    @Override
    public RecordLoadable<? extends Record> loadable() {
      return STAT_TYPE;
    }

    @Override
    public int getRepairAmount(IToolStackView tool) {
      if (repairAmount == -1) {
        repairAmount = getDurability(tool.getDefinition().getId(), material, statType);
      }
      return repairAmount;
    }
  }


  /* Builder */

  public static Builder material(MaterialId material) {
    return new Builder(material);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends ModuleBuilder.Stack<Builder> {
    private final MaterialId material;

    /** Creates a module with a constant repair amount */
    public MaterialRepairModule constant(int repairAmount) {
      return new MaterialRepairModule(material, repairAmount, condition);
    }

    /** Creates a module with a stat type driven repair amount */
    public MaterialRepairModule statType(MaterialStatsId statType) {
      return new StatType(material, statType, condition);
    }
  }
}
