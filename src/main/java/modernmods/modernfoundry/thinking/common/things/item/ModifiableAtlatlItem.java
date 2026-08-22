package modernmods.modernfoundry.thinking.common.things.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.item.ranged.ModifiableBowItem;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tools.modifiers.ability.interaction.BlockingModifier;

public class ModifiableAtlatlItem extends ModifiableBowItem {
    public ModifiableAtlatlItem(Properties properties, ToolDefinition toolDefinition, boolean storeDrawingItem) {
        super(properties, toolDefinition, storeDrawingItem);
    }
   @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return BlockingModifier.blockWhileCharging(ToolStack.from(stack), UseAnim.SPEAR);
    }
}
