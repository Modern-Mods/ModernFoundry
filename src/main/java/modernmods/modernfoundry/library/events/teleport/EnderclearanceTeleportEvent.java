package modernmods.modernfoundry.library.events.teleport;

import net.minecraft.world.entity.Entity;
import modernmods.modernfoundry.compat.neoforged.bus.api.Cancelable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.utils.TeleportHelper.ITeleportEventFactory;
import modernmods.modernfoundry.tools.data.ModifierIds;

/**
 * Event fired when an entity teleports via {@link modernmods.modernfoundry.tools.modules.armor.EnderclearanceModule}
 * @deprecated replacing with {@link ModifierTeleportEvent} in the future.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Cancelable
@Deprecated
public class EnderclearanceTeleportEvent extends ModifierTeleportEvent {
  /** @deprecated use {@link #EnderclearanceTeleportEvent(Entity, double, double, double, ModifierEntry)} */
  @Deprecated(forRemoval = true)
  public static final ITeleportEventFactory TELEPORT_FACTORY = EnderclearanceTeleportEvent::new;

  public EnderclearanceTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, ModifierEntry modifier) {
    super(entity, targetX, targetY, targetZ, modifier);
  }

  /** @deprecated use {@link #EnderclearanceTeleportEvent(Entity, double, double, double, ModifierEntry)} */
  @Deprecated(forRemoval = true)
  public EnderclearanceTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
    this(entity, targetX, targetY, targetZ, new ModifierEntry(ModifierIds.enderclearance, 1));
  }
}
