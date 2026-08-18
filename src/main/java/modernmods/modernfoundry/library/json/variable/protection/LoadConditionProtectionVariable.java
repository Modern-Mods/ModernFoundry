package modernmods.modernfoundry.library.json.variable.protection;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.mantle.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;

/**
 * Datagen helper for making conditional {@link ProtectionVariable}.
 * @param ifTrue      Variable to use if all conditions are true.
 * @param ifFalse     Variable to use if any condition is false.
 * @param conditions  Conditions to evaluate.
 */
@SuppressWarnings("unused") // API
public record LoadConditionProtectionVariable(ProtectionVariable ifTrue, ProtectionVariable ifFalse, ICondition... conditions) implements ProtectionVariable, ConditionalObject<ProtectionVariable> {
  @Override
  public float getValue(IToolStackView tool, @Nullable EquipmentContext context, @Nullable LivingEntity target, @Nullable DamageSource source, @Nullable EquipmentSlot slotType) {
    return (Util.testConditions(conditions) ? ifTrue : ifFalse).getValue(tool, context, target, source, slotType);
  }

  @Override
  public RecordLoadable<? extends ProtectionVariable> getLoader() {
    return ProtectionVariable.LOADER.getConditionalLoader();
  }
}
