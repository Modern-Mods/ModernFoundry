package modernmods.modernfoundry.library.fluid;

import lombok.Getter;
import lombok.Setter;
import modernmods.hilt.block.entity.HiltBlockEntity;

public class FluidTankAnimated extends FluidTankBase<HiltBlockEntity> {
  @Getter @Setter
  private float renderOffset;

  public FluidTankAnimated(int capacity, HiltBlockEntity parent) {
    super(capacity, parent);
  }
}
