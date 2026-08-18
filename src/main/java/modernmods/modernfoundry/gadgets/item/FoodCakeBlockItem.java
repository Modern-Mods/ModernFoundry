package modernmods.modernfoundry.gadgets.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import modernmods.mantle.item.BlockTooltipItem;
import modernmods.modernfoundry.fluids.item.ContainerFoodItem;
import modernmods.modernfoundry.gadgets.block.FoodCakeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Block item for cakes, adding the eaten effects to the tooltip. Block tooltips moved to the item in 26.1. */
public class FoodCakeBlockItem extends BlockTooltipItem {
  public FoodCakeBlockItem(Block block, Properties properties) {
    super(block, properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltipDisplay, tooltipConsumer, flag);
    if (getBlock() instanceof FoodCakeBlock cake) {
      List<Component> tooltip = new ArrayList<>();
      ContainerFoodItem.addEffectTooltip(cake.getConsumable(), tooltip);
      tooltip.forEach(tooltipConsumer);
    }
  }
}
