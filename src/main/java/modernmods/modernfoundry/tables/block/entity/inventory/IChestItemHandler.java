package modernmods.modernfoundry.tables.block.entity.inventory;

import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import modernmods.mantle.block.entity.MantleBlockEntity;

/** Interface for tinker chest TEs */
public interface IChestItemHandler extends IItemHandlerModifiable, ValueIOSerializable, IScalingContainer {
  /** Sets the parent of this block */
  void setParent(MantleBlockEntity parent);
}
