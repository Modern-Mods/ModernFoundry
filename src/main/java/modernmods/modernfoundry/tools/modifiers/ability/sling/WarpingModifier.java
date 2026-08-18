package modernmods.modernfoundry.tools.modifiers.ability.sling;

import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.interaction.sling.SlingTeleportModule;

/** @deprecated use {@link SlingTeleportModule} */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public class WarpingModifier extends SlingModifier {
  private static final SlingTeleportModule WARPING = new SlingTeleportModule(LevelingValue.flat(6), 1.5f, LivingEntityPredicate.ANY, ModifierCondition.ANY_TOOL);
  @Override
  public void beforeReleaseUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
    WARPING.beforeReleaseUsing(tool, modifier, entity, useDuration, timeLeft, activeModifier);
  }
}
