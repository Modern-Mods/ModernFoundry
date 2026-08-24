package modernmods.modernfoundry.world;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerModule;
import modernmods.modernfoundry.shared.TinkerMaterials;
import modernmods.modernfoundry.world.block.OreberryBushBlock;
import modernmods.modernfoundry.world.item.OreberryItem;

/** Native canonical Oreberries content. User-defined runtime registry types are intentionally not supported. */
public final class Oreberries extends TinkerModule {
  public static final ItemObject<OreberryItem> ironBerry = berry("iron_oreberry", "Sweet Irony", 0xFFC7A3, false);
  public static final ItemObject<OreberryItem> goldBerry = berry("gold_oreberry", "Pure Luster", 0xFFCC33, false);
  public static final ItemObject<OreberryItem> copperBerry = berry("copper_oreberry", "Tastes like metal", 0xFF8833, false);
  public static final ItemObject<OreberryItem> tinBerry = berry("tin_oreberry", "Tin Man", 0xBB4422, false);
  public static final ItemObject<OreberryItem> aluminumBerry = berry("aluminum_oreberry", "White Chocolate", 0xEEFFFF, false);
  public static final ItemObject<OreberryItem> essenceBerry = berry("essence_berry", "Tastes like Creeper", 0xFFFFFF, true);

  /** Reuses Modern Foundry's canonical copper nugget instead of creating a duplicate item ID. */
  public static final ItemObject<Item> copperNugget = TinkerMaterials.copperNugget;
  public static final ItemObject<Item> tinNugget = ITEMS.register("tin_nugget", ITEM_PROPS);
  public static final ItemObject<Item> aluminumNugget = ITEMS.register("aluminum_nugget", ITEM_PROPS);

  public static final ItemObject<OreberryBushBlock> ironBush = bush("iron_oreberry_bush", ironBerry, false);
  public static final ItemObject<OreberryBushBlock> goldBush = bush("gold_oreberry_bush", goldBerry, false);
  public static final ItemObject<OreberryBushBlock> copperBush = bush("copper_oreberry_bush", copperBerry, false);
  public static final ItemObject<OreberryBushBlock> tinBush = bush("tin_oreberry_bush", tinBerry, false);
  public static final ItemObject<OreberryBushBlock> aluminumBush = bush("aluminum_oreberry_bush", aluminumBerry, false);
  public static final ItemObject<OreberryBushBlock> essenceBush = bush("essence_berry_bush", essenceBerry, true);

  public static final DeferredHolder<? super CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
    "oreberries", () -> CreativeModeTab.builder()
      .title(TConstruct.makeTranslation("itemGroup", "oreberries"))
      .icon(() -> new ItemStack(ironBush.get()))
      .displayItems(Oreberries::addTabItems)
      .build());

  private static ItemObject<OreberryItem> berry(String id, String tooltip, int color, boolean essence) {
    return ITEMS.register(id, () -> new OreberryItem(ITEM_PROPS, id, color, essence));
  }

  private static ItemObject<OreberryBushBlock> bush(String id, ItemObject<? extends OreberryItem> berry, boolean growsInLight) {
    return BLOCKS.register(id, () -> new OreberryBushBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).sound(SoundType.METAL).strength(0.3F)
        .randomTicks().noOcclusion().pushReaction(PushReaction.DESTROY)
        .isSuffocating((state, level, pos) -> false)
        .isViewBlocking((state, level, pos) -> false),
      berry::get, growsInLight), BLOCK_ITEM);
  }

  private static void addTabItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
    output.accept(ironBerry);
    output.accept(goldBerry);
    output.accept(copperBerry);
    output.accept(tinBerry);
    output.accept(aluminumBerry);
    output.accept(essenceBerry);
    output.accept(copperNugget);
    output.accept(tinNugget);
    output.accept(aluminumNugget);
    output.accept(ironBush);
    output.accept(goldBush);
    output.accept(copperBush);
    output.accept(tinBush);
    output.accept(aluminumBush);
    output.accept(essenceBush);
  }

}
