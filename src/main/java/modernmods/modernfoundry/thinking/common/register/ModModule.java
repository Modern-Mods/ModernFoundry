package modernmods.modernfoundry.thinking.common.register;

import modernmods.hilt.item.BlockTooltipItem;
import modernmods.hilt.item.TooltipItem;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerModule;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;
import java.util.function.Supplier;

/** Shared registration aliases for the native Tinkers' Thinking port. */
public abstract class ModModule extends TinkerModule {
  protected static final Item.Properties GENERAL_PROPS = ITEM_PROPS;
  protected static final Item.Properties FIRE_PROPS = new Item.Properties().fireResistant();
  protected static final Item.Properties Stack1Item = UNSTACKABLE_PROPS;

  protected static final Supplier<Item> TOOLTIP_ITEM = () -> new TooltipItem(GENERAL_PROPS);
  protected static final Function<Block, ? extends BlockItem> GENERAL_BLOCK_ITEM = BLOCK_ITEM;
  protected static final Function<Block, ? extends BlockItem> FIRE_BLOCK_ITEM = block -> new BlockItem(block, FIRE_PROPS);
  protected static final Function<Block, ? extends BlockItem> GENERAL_TOOLTIP_BLOCK_ITEM = TOOLTIP_BLOCK_ITEM;
  protected static final Function<Block, ? extends BlockItem> FIRE_TOOLTIP_BLOCK_ITEM = block -> new BlockTooltipItem(block, FIRE_PROPS);

  /** Registration is owned by Modern Foundry's single shared TinkerModule register set. */
  public static void initRegisters() {
    // TConstruct registers the shared native registers after all modules are constructed.
  }

  protected static String descriptionId(String type, String name) {
    return TConstruct.makeDescriptionId(type, name);
  }
}
