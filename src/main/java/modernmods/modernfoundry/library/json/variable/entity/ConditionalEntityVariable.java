package modernmods.modernfoundry.library.json.variable.entity;

import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.variable.ConditionalVariable;

/**
 * Gets one of two entity properties based on the condition
 */
public record ConditionalEntityVariable(IJsonPredicate<LivingEntity> condition, EntityVariable ifTrue, EntityVariable ifFalse)
    implements EntityVariable, ConditionalVariable<IJsonPredicate<LivingEntity>,EntityVariable> {
  public static final RecordLoadable<ConditionalEntityVariable> LOADER = ConditionalVariable.loadable(LivingEntityPredicate.LOADER, EntityVariable.LOADER, ConditionalEntityVariable::new);

  public ConditionalEntityVariable(IJsonPredicate<LivingEntity> condition, float ifTrue, float ifFalse) {
    this(condition, new EntityVariable.Constant(ifTrue), new EntityVariable.Constant(ifFalse));
  }

  @Override
  public float getValue(LivingEntity entity) {
    return condition.matches(entity) ? ifTrue.getValue(entity) : ifFalse.getValue(entity);
  }

  @Override
  public RecordLoadable<ConditionalEntityVariable> getLoader() {
    return LOADER;
  }
}
