package modernmods.modernfoundry.tools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.item.ModifiableItem;

public class ModifiableSwordItem extends ModifiableItem {
  public ModifiableSwordItem(Properties properties, ToolDefinition toolDefinition) {
    super(properties, toolDefinition);
  }

  public ModifiableSwordItem(Properties properties, ToolDefinition toolDefinition, int maxStackSize) {
    super(properties, toolDefinition, maxStackSize);
  }

  @Override
  public boolean canDestroyBlock(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, net.minecraft.world.entity.LivingEntity user) {
    // swords cannot break blocks in creative (26.1.2 renamed canAttackBlock to canDestroyBlock)
    return !(user instanceof Player player && player.getAbilities().instabuild);
  }
}
