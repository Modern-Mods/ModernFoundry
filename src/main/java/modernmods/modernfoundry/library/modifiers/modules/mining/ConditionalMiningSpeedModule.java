package modernmods.modernfoundry.library.modifiers.modules.mining;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.hilt.data.loadable.primitive.BooleanLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.block.BlockPredicate;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.math.ModifierFormula;
import modernmods.modernfoundry.library.json.variable.VariableFormula;
import modernmods.modernfoundry.library.json.variable.mining.MiningSpeedFormula;
import modernmods.modernfoundry.library.json.variable.mining.MiningSpeedVariable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedContext;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ConditionalStatTooltip;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Implementation of attack damage conditioned on the attacker or target's properties
 * @param block      Blocks to boost speed
 * @param holder     Condition on the entity holding this tool
 * @param formula    Damage formula
 * @param condition  Standard modifier conditions
 */
public record ConditionalMiningSpeedModule(
  IJsonPredicate<BlockState> block, IJsonPredicate<LivingEntity> holder, boolean requireEffective,
  MiningSpeedFormula formula, ModifierCondition<IToolStackView> condition
) implements BreakSpeedModifierHook, ConditionalStatTooltip, ModifierModule, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ConditionalMiningSpeedModule>defaultHooks(ModifierHooks.BREAK_SPEED, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<ConditionalMiningSpeedModule> LOADER = RecordLoadable.create(
    BlockPredicate.LOADER.defaultField("blocks", ConditionalMiningSpeedModule::block),
    LivingEntityPredicate.LOADER.defaultField("entity", ConditionalMiningSpeedModule::holder),
    BooleanLoadable.INSTANCE.defaultField("require_effective", true, ConditionalMiningSpeedModule::requireEffective),
    MiningSpeedFormula.LOADER.directField(ConditionalMiningSpeedModule::formula),
    ModifierCondition.TOOL_FIELD,
    ConditionalMiningSpeedModule::new);

  /** @apiNote Internal constructor, use {@link #builder()} */
  @Internal
  public ConditionalMiningSpeedModule {}

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

  @SuppressWarnings("removal")
  @Override
  public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    Player player = event.getEntity();
    if ((isEffective || !requireEffective) && condition.matches(tool, modifier) && block.matches(event.getState()) && holder.matches(player)) {
      event.setNewSpeed(formula.apply(tool, modifier, event, player, sideHit, event.getOriginalSpeed(), event.getNewSpeed(), miningSpeedModifier));
    }
  }

  @Override
  public float modifyBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeedContext context, float speed) {
    Player player = context.player();
    if ((!requireEffective || context.isEffective()) && condition.matches(tool, modifier) && block.matches(context.state()) && holder.matches(player)) {
      return formula.apply(tool, modifier, context, player, context.originalSpeed(), speed, context.miningSpeedMultiplier());
    }
    return speed;
  }

  @Override
  public INumericToolStat<?> stat() {
    return ToolStats.MINING_SPEED;
  }

  @Override
  public float computeTooltipValue(IToolStackView tool, ModifierEntry entry, @Nullable Player player) {
    return formula.apply(tool, entry, null, player, 1, 1, 1);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ConditionalMiningSpeedModule> getLoader() {
    return LOADER;
  }


  /* Builder */

  /** Creates a builder instance */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder class */
  @Accessors(fluent = true)
  public static class Builder extends VariableFormula.Builder<Builder,ConditionalMiningSpeedModule,MiningSpeedVariable> {
    @Setter
    private IJsonPredicate<BlockState> blocks = BlockPredicate.ANY;
    @Setter
    private IJsonPredicate<LivingEntity> holder = LivingEntityPredicate.ANY;
    private boolean requireEffective = true;

    private Builder() {
      super(MiningSpeedFormula.VARIABLES);
    }

    /** Sets this to a percent boost formula */
    public Builder allowIneffective() {
      this.requireEffective = false;
      return this;
    }

    @Override
    protected ConditionalMiningSpeedModule build(ModifierFormula formula) {
      return new ConditionalMiningSpeedModule(blocks, holder, requireEffective, new MiningSpeedFormula(formula, variables, percent), condition);
    }
  }
}
