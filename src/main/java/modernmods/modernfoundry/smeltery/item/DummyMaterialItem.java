package modernmods.modernfoundry.smeltery.item;

import net.minecraft.ChatFormatting;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import modernmods.modernfoundry.TConstruct;

import java.util.List;

/** Item for creating casts that looks like a tool part */
public class DummyMaterialItem extends Item {
  private static final Component DUMMY_TOOL_PART = TConstruct.makeTranslation("item", "dummy_tool_part.tooltip").withStyle(ChatFormatting.GRAY);
  public DummyMaterialItem(Properties pProperties) {
    super(pProperties);
  }

  @Override
  public void appendHoverText(ItemStack pStack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag pIsAdvanced) {
    List<Component> tooltip = new java.util.ArrayList<>();
    tooltip.add(DUMMY_TOOL_PART);
  
    tooltip.forEach(tooltipConsumer);
  }
}
