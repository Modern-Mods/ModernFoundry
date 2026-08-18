package modernmods.modernfoundry.library.modifiers.modules.behavior;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.math.ModifierFormula;
import modernmods.modernfoundry.library.json.variable.VariableFormula;
import modernmods.modernfoundry.library.json.variable.stat.ConditionalStatFormula;
import modernmods.modernfoundry.library.json.variable.stat.ConditionalStatVariable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ConditionalStatTooltip;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module for common conditional stats, such as on ranged tools
 * @param stat        Stat to boost
 * @param holder      Condition on the tool holder
 * @param formula     Formula to apply
 * @param condition   Standard modifier module conditions
 */
public record ConditionalStatModule(INumericToolStat<?> stat, IJsonPredicate<LivingEntity> holder, ConditionalStatFormula formula, ModifierCondition<IToolStackView> condition) implements ModifierModule, ConditionalStatModifierHook, ConditionalStatTooltip, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ConditionalStatModule>defaultHooks(ModifierHooks.CONDITIONAL_STAT, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<ConditionalStatModule> LOADER = RecordLoadable.create(
    ToolStats.NUMERIC_LOADER.requiredField("stat", ConditionalStatModule::stat),
    LivingEntityPredicate.LOADER.defaultField("entity", ConditionalStatModule::holder),
    ConditionalStatFormula.LOADER.directField(ConditionalStatModule::formula),
    ModifierCondition.TOOL_FIELD,
    ConditionalStatModule::new);

  /** @apiNote Internal constructor, use {@link #stat(INumericToolStat)} */
  @Internal
  public ConditionalStatModule {}

  @Override
  public boolean percent() {
    return formula.percent();
  }

  @Nullable
  @Override
  public Integer getPriority() {
    // run multipliers a bit later
    return percent() ? 75 : null;
  }

  @Override
  public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
    if (this.stat == stat && condition.matches(tool, modifier) && this.holder.matches(living)) {
      return formula.apply(tool, modifier, living, baseValue, multiplier);
    }
    return baseValue;
  }

  @Override
  public float computeTooltipValue(IToolStackView tool, ModifierEntry entry, @Nullable Player player) {
    return formula.apply(tool, entry, player, 1, tool.getMultiplier(stat));
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ConditionalStatModule> getLoader() {
    return LOADER;
  }


  /* Builder */

  /** Creates a builder instance */
  public static Builder stat(INumericToolStat<?> stat) {
    return new Builder(stat);
  }

  /** Builder class */
  public static class Builder extends VariableFormula.Builder<Builder,ConditionalStatModule,ConditionalStatVariable> {
    private final INumericToolStat<?> stat;
    @Setter
    @Accessors(fluent = true)
    private IJsonPredicate<LivingEntity> holder = LivingEntityPredicate.ANY;

    private Builder(INumericToolStat<?> stat) {
      super(ConditionalStatFormula.VARIABLES);
      this.stat = stat;
    }

    @Override
    protected ConditionalStatModule build(ModifierFormula formula) {
      return new ConditionalStatModule(stat, holder, new ConditionalStatFormula(formula, variables, percent), condition);
    }
  }
}
