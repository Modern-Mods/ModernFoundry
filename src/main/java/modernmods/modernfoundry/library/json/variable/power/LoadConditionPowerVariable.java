package modernmods.modernfoundry.library.json.variable.power;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.hilt.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;

/**
 * Datagen helper for making conditional {@link PowerVariable}.
 * @param ifTrue      Variable to use if all conditions are true.
 * @param ifFalse     Variable to use if any condition is false.
 * @param conditions  Conditions to evaluate.
 */
@SuppressWarnings("unused") // API
public record LoadConditionPowerVariable(PowerVariable ifTrue, PowerVariable ifFalse, ICondition... conditions) implements PowerVariable, ConditionalObject<PowerVariable> {
  @Override
  public float getValue(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, @Nullable Projectile projectile, @Nullable EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    return (Util.testConditions(conditions) ? ifTrue : ifFalse).getValue(modifiers, persistentData, modifier, projectile, hit, attacker, target);
  }

  @Override
  public RecordLoadable<? extends PowerVariable> getLoader() {
    return PowerVariable.LOADER.getConditionalLoader();
  }
}
