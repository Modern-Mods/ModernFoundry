package modernmods.modernfoundry.library.json.variable.melee;

import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.math.ModifierFormula;
import modernmods.modernfoundry.library.json.variable.VariableFormula;
import modernmods.modernfoundry.library.json.variable.VariableFormulaLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.modules.combat.ConditionalMeleeDamageModule;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/** Variable context for {@link ConditionalMeleeDamageModule} */
public record MeleeFormula(ModifierFormula formula, List<MeleeVariable> variables, String[] variableNames, boolean percent) implements VariableFormula<MeleeVariable> {
  /** Variables for the modifier formula */
  public static final String[] VARIABLES = { "level", "damage", "multiplier", "base_damage" };
  /** Loader instance */
  public static final RecordLoadable<MeleeFormula> LOADER = new VariableFormulaLoadable<>(MeleeVariable.LOADER, VARIABLES, (formula, variables, percent) -> new MeleeFormula(formula, variables, EMPTY_STRINGS, percent));

  public MeleeFormula(ModifierFormula formula, Map<String,MeleeVariable> variables, boolean percent) {
    this(formula, List.copyOf(variables.values()), VariableFormula.getNames(variables), percent);
  }

  /** Builds the arguments from the context */
  private float[] getArguments(IToolStackView tool, ModifierEntry modifier, @Nullable ToolAttackContext context, @Nullable LivingEntity attacker, float baseDamage, float damage) {
    int size = variables.size();
    float[] arguments = VariableFormula.statModuleArguments(size, formula.processLevel(modifier), baseDamage, damage, tool.getMultiplier(ToolStats.ATTACK_DAMAGE));
    for (int i = 0; i < size; i++) {
      arguments[4+i] = variables.get(i).getValue(tool, context, attacker);
    }
    return arguments;
  }

  /** Runs this formula */
  public float apply(IToolStackView tool, ModifierEntry modifier, @Nullable ToolAttackContext context, @Nullable LivingEntity attacker, float baseDamage, float damage) {
    return formula.apply(getArguments(tool, modifier, context, attacker, baseDamage, damage));
  }
}
