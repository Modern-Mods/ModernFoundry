package modernmods.modernfoundry.library.json.variable.entity;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.hilt.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.variable.tool.ToolVariable;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

/**
 * Datagen helper for making conditional {@link EntityVariable}.
 * @param ifTrue      Variable to use if all conditions are true.
 * @param ifFalse     Variable to use if any condition is false.
 * @param conditions  Conditions to evaluate.
 */
@SuppressWarnings("unused") // API
public record LoadConditionEntityVariable(EntityVariable ifTrue, EntityVariable ifFalse, ICondition... conditions) implements EntityVariable, ConditionalObject<EntityVariable> {
  @Override
  public float getValue(LivingEntity entity) {
    return (Util.testConditions(conditions) ? ifTrue : ifFalse).getValue(entity);
  }

  @Override
  public RecordLoadable<? extends EntityVariable> getLoader() {
    return EntityVariable.LOADER.getConditionalLoader();
  }
}
