package modernmods.modernfoundry.library.events.teleport;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import modernmods.modernfoundry.compat.neoforged.bus.api.Cancelable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/** Event fired when an entity teleports using the ender sling modifier */
@Cancelable
@Getter
public class SlingModifierTeleportEvent extends ModifierTeleportEvent {
  private final IToolStackView tool;
  public SlingModifierTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, IToolStackView tool, ModifierEntry entry) {
    super(entity, targetX, targetY, targetZ, entry);
    this.tool = tool;
  }

  /** @deprecated use {@link #getModifier()} */
  @Deprecated
  public ModifierEntry getEntry() {
    return getModifier();
  }
}
