package modernmods.modernfoundry.shared.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import modernmods.mantle.item.BlockTooltipItem;
import modernmods.mantle.util.RetexturedHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Block item that shows the retextured block name in its tooltip.
 * <p>
 * In 26.1.2 vanilla removed block-level {@code appendHoverText} (BlockItem no longer delegates tooltips to the block),
 * so the retextured tooltip formerly provided by {@code RetexturedBlock}/{@code RetexturedTableBlock} lives here instead.
 */
public class RetexturedBlockItem extends BlockTooltipItem {
  public RetexturedBlockItem(Block block, Item.Properties props) {
    super(block, props);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, display, tooltip, flag);
    List<Component> retextured = new ArrayList<>();
    RetexturedHelper.addTooltip(stack, retextured, flag);
    retextured.forEach(tooltip);
  }
}
