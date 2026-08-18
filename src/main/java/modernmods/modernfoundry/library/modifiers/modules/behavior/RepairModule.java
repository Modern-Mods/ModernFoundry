package modernmods.modernfoundry.library.modifiers.modules.behavior;

import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.math.FormulaLoadable;
import modernmods.modernfoundry.library.json.math.ModifierFormula;
import modernmods.modernfoundry.library.json.math.ModifierFormula.FallbackFormula;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.RepairFactorModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module for multiplying tool repair */
public record RepairModule(ModifierFormula formula, ModifierCondition<IToolStackView> condition) implements RepairFactorModifierHook, ModifierModule, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<RepairModule>defaultHooks(ModifierHooks.REPAIR_FACTOR);
  public static final int FACTOR = 1;
  /** Formula instance for the loader */
  private static final FormulaLoadable FORMULA = new FormulaLoadable(FallbackFormula.PERCENT, "level", "factor");
  /** Loader instance */
  public static final RecordLoadable<RepairModule> LOADER = RecordLoadable.create(FORMULA.directField(RepairModule::formula), ModifierCondition.TOOL_FIELD, RepairModule::new);

  /** Creates a builder instance */
  public static FormulaLoadable.Builder<RepairModule> builder() {
    return FORMULA.builder(RepairModule::new);
  }

  /** @apiNote Internal constructor, use {@link #builder()} */
  @Internal
  public RepairModule {}

  @Override
  public float getRepairFactor(IToolStackView tool, ModifierEntry entry, float factor) {
    if (condition.matches(tool, entry)) {
      return formula.apply(formula.processLevel(entry), factor);
    }
    return factor;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<RepairModule> getLoader() {
    return LOADER;
  }
}
