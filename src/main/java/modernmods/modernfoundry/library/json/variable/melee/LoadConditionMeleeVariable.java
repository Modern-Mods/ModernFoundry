package modernmods.modernfoundry.library.json.variable.melee;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.mantle.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;

/**
 * Datagen helper for making conditional {@link MeleeVariable}.
 * @param ifTrue      Variable to use if all conditions are true.
 * @param ifFalse     Variable to use if any condition is false.
 * @param conditions  Conditions to evaluate.
 */
@SuppressWarnings("unused") // API
public record LoadConditionMeleeVariable(MeleeVariable ifTrue, MeleeVariable ifFalse, ICondition... conditions) implements MeleeVariable, ConditionalObject<MeleeVariable> {
  @Override
  public float getValue(IToolStackView tool, @Nullable ToolAttackContext context, @Nullable LivingEntity attacker) {
    return (Util.testConditions(conditions) ? ifTrue : ifFalse).getValue(tool, context, attacker);
  }

  @Override
  public RecordLoadable<? extends MeleeVariable> getLoader() {
    return MeleeVariable.LOADER.getConditionalLoader();
  }
}
