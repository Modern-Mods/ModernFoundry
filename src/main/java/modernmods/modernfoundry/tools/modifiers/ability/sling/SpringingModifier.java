package modernmods.modernfoundry.tools.modifiers.ability.sling;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.interaction.sling.SlingLeapModule;

/** @deprecated use {@link SlingLeapModule} */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public class SpringingModifier extends SlingModifier {
  private static final SlingLeapModule SPRINGING = new SlingLeapModule(LevelingValue.flat(1.05f), true, 1.0f, 2, true, LivingEntityPredicate.ELYTRA_FLYING.inverted(), ModifierCondition.ANY_TOOL);

  @Override
  public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
    return SPRINGING.onToolUse(tool, modifier, player, hand, source);
  }

  @Override
  public void beforeReleaseUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
    SPRINGING.beforeReleaseUsing(tool, modifier, entity, useDuration, timeLeft, activeModifier);
  }
}
