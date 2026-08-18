package modernmods.modernfoundry.library.json.variable.entity;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.TinkerLoadables;

import java.util.List;

/** Variable that counts the number of slots with equipment matching the list of slots passed */
public record EquipmentCountEntityVariable(List<EquipmentSlot> slots) implements EntityVariable {
  public static final RecordLoadable<EquipmentCountEntityVariable> LOADER = RecordLoadable.create(TinkerLoadables.EQUIPMENT_SLOT.list(1).requiredField("slots", EquipmentCountEntityVariable::slots), EquipmentCountEntityVariable::new);

  public EquipmentCountEntityVariable(EquipmentSlot... slots) {
    this(List.of(slots));
  }

  @Override
  public RecordLoadable<? extends EntityVariable> getLoader() {
    return LOADER;
  }

  @Override
  public float getValue(LivingEntity entity) {
    int count = 0;
    for (EquipmentSlot slot : slots) {
      if (!entity.getItemBySlot(slot).isEmpty()) {
        count += 1;
      }
    }
    return count;
  }
}
