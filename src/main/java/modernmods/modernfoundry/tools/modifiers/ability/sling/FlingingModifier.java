package modernmods.modernfoundry.tools.modifiers.ability.sling;

import net.minecraft.world.entity.LivingEntity;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.json.predicate.TinkerPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.interaction.sling.SlingLeapModule;

/** @deprecated use {@link SlingLeapModule} */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public class FlingingModifier extends SlingModifier {
  private static final SlingLeapModule FLINGING = new SlingLeapModule(LevelingValue.flat(4), false, 1.5f, 3, false, LivingEntityPredicate.and(LivingEntityPredicate.ON_GROUND, TinkerPredicate.TARGETING_BLOCK), ModifierCondition.ANY_TOOL);

  @Override
  public void beforeReleaseUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
    FLINGING.beforeReleaseUsing(tool, modifier, entity, useDuration, timeLeft, activeModifier);
  }
}
