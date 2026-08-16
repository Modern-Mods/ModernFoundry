package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.shared.TinkerAttributes;
import modernmods.modernfoundry.tools.modules.ReduceEffectOnUnequipModule;

/** @deprecated use {@link TinkerAttributes#GOOD_EFFECT_DURATION} and {@link ReduceEffectOnUnequipModule} */
@Deprecated(forRemoval = true)
public class BoonOfSssssModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(AttributeModule.builder(TinkerAttributes.GOOD_EFFECT_DURATION, Operation.ADD_MULTIPLIED_BASE).eachLevel(0.25f));
    hookBuilder.addModule(new ReduceEffectOnUnequipModule(MobEffectCategory.BENEFICIAL, LevelingValue.eachLevel(0.2f), ModifierCondition.ANY_TOOL));
  }
}
