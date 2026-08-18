package modernmods.modernfoundry.tools.modifiers.effect;

import net.minecraft.world.effect.MobEffectCategory;
import modernmods.modernfoundry.common.TinkerEffect;

/**
 * Effect that cannot be cured with milk.
 * The NeoForge per-effect cure opt-out ({@code fillEffectCures}/{@code EffectCure}) was removed in 26.1.2;
 * milk curing is now controlled entirely by the consumable item's remove-effects consume effect, so there is
 * nothing for this effect to override. Retained as a base class for effects that historically resisted milk.
 * TODO 1.21: move to {@link modernmods.modernfoundry.shared.effect}
 */
public class NoMilkEffect extends TinkerEffect {
  public NoMilkEffect(MobEffectCategory typeIn, int color, boolean show) {
    super(typeIn, color, show);
  }
}
