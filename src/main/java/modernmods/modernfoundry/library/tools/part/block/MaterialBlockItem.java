package modernmods.modernfoundry.library.tools.part.block;

import net.minecraft.nbt.CompoundTag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.library.tools.part.MaterialItem;
import modernmods.modernfoundry.library.utils.TagUtil;

import javax.annotation.Nullable;
import java.util.List;

/** Implementation of {@link MaterialItem} on a {@link BlockItem}. */
public class MaterialBlockItem extends BlockItem implements IMaterialItem {
  public MaterialBlockItem(Block block, Properties properties) {
    super(block, properties);
  }

  @Override
  public MaterialVariantId getMaterial(ItemStack stack) {
    return MaterialItem.getMaterialId(TagUtil.getTag(stack));
  }

  @Override
  public Component getName(ItemStack stack) {
    return MaterialItem.getName(this, stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    List<Component> tooltip = new java.util.ArrayList<>();
    MaterialItem.appendHoverText(this, stack, tooltip, flag);
    super.appendHoverText(stack, context, tooltipDisplay, tooltip::add, flag);
  
    tooltip.forEach(tooltipConsumer);
  }

  @Nullable
  @Override
  public String getCreatorModId(net.minecraft.core.HolderLookup.Provider registries, ItemStack stack) {
    return MaterialItem.getCreatorModId(this, stack);
  }

  public void verifyTagAfterLoad(CompoundTag tag) {
    MaterialItem.verifyTag(tag);
  }
}
