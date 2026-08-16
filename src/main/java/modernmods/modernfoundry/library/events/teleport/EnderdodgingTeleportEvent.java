package modernmods.modernfoundry.library.events.teleport;

import net.minecraft.world.entity.Entity;
import modernmods.modernfoundry.compat.neoforged.bus.api.Cancelable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.tools.data.ModifierIds;

/**
 * Event fired when an entity teleports using {@link modernmods.modernfoundry.tools.modifiers.traits.skull.EnderdodgingModifier}
 * @deprecated replacing with {@link ModifierTeleportEvent} in the future.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Cancelable
@Deprecated
public class EnderdodgingTeleportEvent extends ModifierTeleportEvent {
  public EnderdodgingTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, ModifierEntry modifier) {
    super(entity, targetX, targetY, targetZ, modifier);
  }

  /** @deprecated use {@link #EnderdodgingTeleportEvent(Entity, double, double, double, ModifierEntry)} */
  @Deprecated(forRemoval = true)
  public EnderdodgingTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
    this(entity, targetX, targetY, targetZ, new ModifierEntry(ModifierIds.enderclearance, 1));
  }
}
