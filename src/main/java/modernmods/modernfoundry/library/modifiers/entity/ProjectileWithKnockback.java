package modernmods.modernfoundry.library.modifiers.entity;

/** Interface for projectiles to receive knockback from the modifier */
public interface ProjectileWithKnockback {
  /** Adds the given knockback amount. Called by {@link modernmods.modernfoundry.tools.modules.ranged.common.PunchModule} */
  void addKnockback(float amount);
}
