package modernmods.modernfoundry.tools.modules.durability;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.predicate.TinkerPredicate;
import modernmods.modernfoundry.library.json.predicate.modifier.ModifierPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module to set a minimum or a maximum amount of damage for a tool to take each operation.
 * @param min    Minimum amount of damage the tool can take. Use 0 for no min.
 * @param max    Maximum amount of damage the tool can take. Use {@link Integer#MAX_VALUE} for no max.
 * @param cause  Condition on the modifier causing the damage. Note that {@link ModifierId#EMPTY} is a valid cause.
 * @param holder Condition on the entity holding the tool.
 * @param condition  Condition of when this module applies
 */
public record ToolDamageRangeModule(int min, int max, IJsonPredicate<ModifierId> cause, IJsonPredicate<LivingEntity> holder, ModifierCondition<IToolStackView> condition) implements ModifierModule, ToolDamageModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ToolDamageRangeModule>defaultHooks(ModifierHooks.TOOL_DAMAGE);
  public static final RecordLoadable<ToolDamageRangeModule> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ZERO.defaultField("min", 0, ToolDamageRangeModule::min),
    IntLoadable.FROM_ZERO.defaultField("max", Integer.MAX_VALUE, ToolDamageRangeModule::max),
    ModifierPredicate.LOADER.defaultField("cause", ToolDamageRangeModule::cause),
    LivingEntityPredicate.LOADER.defaultField("holder", ToolDamageRangeModule::holder),
    ModifierCondition.TOOL_FIELD,
    ToolDamageRangeModule::new);

  public ToolDamageRangeModule(int min, int max, IJsonPredicate<ModifierId> cause) {
    this(min, max, cause, LivingEntityPredicate.ANY, ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<ToolDamageRangeModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Deprecated
  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
    return onDamageTool(tool, modifier, amount, holder, null, ModifierId.EMPTY);
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder, @Nullable ItemStack stack, ModifierId cause) {
    if (condition.matches(tool, modifier) && this.cause.matches(cause) && TinkerPredicate.matches(this.holder, holder)) {
      // if the range is closed, all actions deal that
      if (min == max) {
        return min;
      }
      // ensure amount is in the range
      return Mth.clamp(amount, min, max);
    }
    return amount;
  }
}
