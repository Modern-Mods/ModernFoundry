package modernmods.modernfoundry.library.events.teleport;

import net.minecraft.server.level.ServerLevel;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import modernmods.modernfoundry.compat.neoforged.bus.api.Cancelable;

/** @deprecated No longer used. See {@link SlingModifierTeleportEvent} */
@Deprecated(forRemoval = true)
@Cancelable
public class SlimeslingTeleportEvent extends EntityTeleportEvent {
  @Getter
  private final ItemStack sling;
  public SlimeslingTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, ItemStack sling) {
    super(entity, (ServerLevel) entity.level(), targetX, targetY, targetZ);
    this.sling = sling;
  }
}
