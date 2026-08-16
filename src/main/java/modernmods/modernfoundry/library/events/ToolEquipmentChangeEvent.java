package modernmods.modernfoundry.library.events;

import lombok.Getter;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;

/** Event fired at the end of {@link EquipmentChangeContext}, contains parsed Tinker Tools for all armor and also fires on the client */
public class ToolEquipmentChangeEvent extends LivingEvent {
  @Getter
  private final EquipmentChangeContext context;
  public ToolEquipmentChangeEvent(EquipmentChangeContext context) {
    super(context.getEntity());
    this.context = context;
  }
}
