package modernmods.modernfoundry.library.tools.context;

import lombok.Data;
import lombok.With;
import net.minecraft.world.item.Item;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.nbt.IModDataView;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.MaterialNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

/**
 * Implementation of the limited view of {@link IToolStackView} for use in tool rebuild hooks
 */
@SuppressWarnings("ClassCanBeRecord")
@Data
public class ToolRebuildContext implements IToolContext {
  /** Item being rebuilt */
  private final Item item;
  /** Tool definition of the item being rebuilt */
  private final ToolDefinition definition;
  /** Materials on the tool being rebuilt */
  private final MaterialNBT materials;
  /** List of recipe modifiers on the tool being rebuilt */
  private final ModifierNBT upgrades;
  /** List of all modifiers on the tool being rebuilt, from recipes and traits */
  @With
  private final ModifierNBT modifiers;
  /** Persistent modifier data, intentionally read only */
  private final IModDataView persistentData;
}
