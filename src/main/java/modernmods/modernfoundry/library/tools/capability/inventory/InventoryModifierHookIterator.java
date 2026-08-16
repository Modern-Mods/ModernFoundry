package modernmods.modernfoundry.library.tools.capability.inventory;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.capability.CompoundIndexHookIterator;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolFluidCapability;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability.InventoryModifierHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/**
 * Shared logic to iterate fluid capabilities for {@link ToolFluidCapability}
 */
abstract class InventoryModifierHookIterator<I> extends CompoundIndexHookIterator<InventoryModifierHook,I> {
  /** Entry from {@link #findHook(IToolStackView, int)}, will be set during or before iteration */
  protected ModifierEntry indexEntry = null;

  @Override
  protected int getSize(IToolStackView tool, InventoryModifierHook hook) {
    return hook.getSlots(tool, indexEntry);
  }
}
