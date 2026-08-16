package modernmods.modernfoundry.library.modifiers.modules.behavior;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.hilt.client.TooltipKey;
import modernmods.hilt.data.loadable.primitive.BooleanLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.math.ModifierFormula;
import modernmods.modernfoundry.library.json.predicate.modifier.ModifierPredicate;
import modernmods.modernfoundry.library.json.variable.VariableFormula;
import modernmods.modernfoundry.library.json.variable.stat.ConditionalStatFormula;
import modernmods.modernfoundry.library.json.variable.stat.ConditionalStatVariable;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

import static modernmods.modernfoundry.TConstruct.RANDOM;

/**
 * Module which reduces damage on a tool by a given percentage
 * @param formula    Formula to use
 * @param holder     Condition on entity holding the tool
 * @param cause      Predicate on modifier that caused the tool durability loss.
 * @param condition  Condition for this module to run
 * @param reinforcedTooltip  If true, tooltip shows a custom lang key for reduction instead of just the modifier name
 */
public record ReduceToolDamageModule(IJsonPredicate<LivingEntity> holder, IJsonPredicate<ModifierId> cause, ConditionalStatFormula formula, boolean reinforcedTooltip, ModifierCondition<IToolStackView> condition) implements ModifierModule, ToolDamageModifierHook, TooltipModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ReduceToolDamageModule>defaultHooks(ModifierHooks.TOOL_DAMAGE, ModifierHooks.TOOLTIP);
  /** Loader instance */
  public static final RecordLoadable<ReduceToolDamageModule> LOADER = RecordLoadable.create(
    LivingEntityPredicate.LOADER.defaultField("entity", ReduceToolDamageModule::holder),
    ModifierPredicate.LOADER.defaultField("cause", ReduceToolDamageModule::cause),
    ConditionalStatFormula.IDENTITY_LOADER.directField(ReduceToolDamageModule::formula),
    BooleanLoadable.INSTANCE.defaultField("reinforced_tooltip", false, false, ReduceToolDamageModule::reinforcedTooltip),
    ModifierCondition.TOOL_FIELD,
    ReduceToolDamageModule::new);

  /** Creates a builder instance */
  public static Builder builder() {
    return new Builder();
  }

  /** @apiNote Internal constructor, use {@link #builder()} */
  @Internal
  public ReduceToolDamageModule {}

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /**
   * Damages the given amount with the reinforced percentage
   * @param amount      Amount to damage
   * @param percentage  Percentage of damage to cancel, runs probabilistically
   * @return  Amount after reinforced
   */
  public static int reduceDamage(int amount, float percentage) {
    // 100% protection? all damage blocked
    if (percentage >= 1) {
      return 0;
    }
    // 0% protection? nothing blocked
    if (percentage <= 0) {
      return amount;
    }
    // no easy closed form formula for this that I know of, and damage amount tends to be small, so take a chance for each durability
    int dealt = 0;
    for (int i = 0; i < amount; i++) {
      if (RANDOM.nextFloat() >= percentage) {
        dealt++;
      }
    }
    return dealt;
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
    if (this.condition.matches(tool, modifier)) {
      return reduceDamage(amount, formula.apply(tool, modifier, holder, 0, 1));
    }
    return amount;
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder, @Nullable ItemStack stack, ModifierId cause) {
    if (this.condition.matches(tool, modifier) && this.cause.matches(cause)) {
      return reduceDamage(amount, formula.apply(tool, modifier, holder, 0, 1));
    }
    return amount;
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    if (this.condition.matches(tool, entry)) {
      float percent = formula.apply(tool, entry, tooltipKey == TooltipKey.SHIFT ? player : null, 0, 1);
      if (percent > 0) {
        Modifier modifier = entry.getModifier();
        Component name = reinforcedTooltip ? Component.translatable(modifier.getTranslationKey() + ".reinforced") : modifier.getDisplayName();
        tooltip.add(modifier.applyStyle(Component.literal(Util.PERCENT_FORMAT.format(percent) + " ").append(name)));
      }
    }
  }

  @Override
  public RecordLoadable<ReduceToolDamageModule> getLoader() {
    return LOADER;
  }


  @Setter
  @Accessors(fluent = true)
  public static class Builder extends VariableFormula.Builder<Builder,ReduceToolDamageModule, ConditionalStatVariable> {
    private IJsonPredicate<LivingEntity> holder = LivingEntityPredicate.ANY;
    private IJsonPredicate<ModifierId> cause = ModifierPredicate.ANY;
    private boolean reinforcedTooltip = false;

    private Builder() {
      super(ConditionalStatFormula.VARIABLES);
    }

    /** @deprecated Has no effect */
    @Deprecated(forRemoval = true)
    @Override
    public Builder percent() {
      return this;
    }

    /** Sets the tooltip style to suffix the lang key for the tooltip instead of using the modifier name */
    public Builder reinforcedTooltip() {
      this.reinforcedTooltip = true;
      return this;
    }

    @Override
    protected ReduceToolDamageModule build(ModifierFormula formula) {
      return new ReduceToolDamageModule(holder, cause, new ConditionalStatFormula(formula, variables, percent), reinforcedTooltip, condition);
    }
  }
}
