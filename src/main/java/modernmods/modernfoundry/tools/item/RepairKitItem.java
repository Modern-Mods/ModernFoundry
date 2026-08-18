package modernmods.modernfoundry.tools.item;

import net.minecraft.ChatFormatting;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import modernmods.mantle.util.TranslationHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipe;
import modernmods.modernfoundry.library.tools.definition.module.material.MaterialRepairToolHook;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.part.IRepairKitItem;
import modernmods.modernfoundry.library.tools.part.MaterialItem;
import modernmods.modernfoundry.tools.stats.StatlessMaterialStats;

import java.util.List;

public class RepairKitItem extends MaterialItem implements IRepairKitItem {
  private static final String TOOLTIP_KEY = TConstruct.makeTranslationKey("item", "repair_kit.tooltip");
  private final float repairAmount;
  public RepairKitItem(Properties properties, float repairAmount) {
    super(properties);
    this.repairAmount = repairAmount;
  }

  /** Constructor using config for repair amount */
  public RepairKitItem(Properties properties) {
    this(properties, 0);
  }

  @Override
  public boolean canUseMaterial(MaterialId material) {
    return MaterialRegistry.getInstance()
                           .getAllStats(material)
                           .stream()
                           .anyMatch(stats -> stats == StatlessMaterialStats.REPAIR_KIT || stats.getType().canRepair());
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltipDisplay, tooltipConsumer, flag);
    // tooltip is about inventory repair
    if (canRepairInCraftingTable()) {
      tooltipConsumer.accept(Component.translatable(TOOLTIP_KEY, TranslationHelper.COMMA_FORMAT.format(getRepairAmount())).withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public float getRepairAmount() {
    if (repairAmount == 0) {
      return Config.COMMON.repairKitAmount.get().floatValue();
    }
    return repairAmount;
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
    // stacking on a tool repairs the tool, if the material is valid
    if (canRepairInCraftingTable() && action == ClickAction.SECONDARY && slot.allowModification(player)) {
      // tool must be modifiable, if so block interactions beyond repair
      ItemStack toolItem = slot.getItem();
      if (!toolItem.isEmpty() && toolItem.is(TinkerTags.Items.MODIFIABLE)) {
        ToolStack tool = ToolStack.from(toolItem);
        MaterialId material = getMaterial(stack).getId();
        // tool must be damaged for us to repair it, and we must have a material
        if (tool.getDamage() > 0 && material != IMaterial.UNKNOWN_ID) {
          // ask the tool how much this material is worth
          float amount = MaterialRepairToolHook.repairAmount(tool, material);
          if (amount > 0) {
            // if its worth anything, add in repair kit value, then ask modifiers to change the amount
            amount *= getRepairAmount() / MaterialRecipe.INGOTS_PER_REPAIR;
            for (ModifierEntry entry : tool.getModifierList()) {
              amount = entry.getHook(ModifierHooks.REPAIR_FACTOR).getRepairFactor(tool, entry, amount);
              if (amount <= 0) {
                return true;
              }
            }
            // assuming no modifier said no repair, we are good, time to repair
            ToolDamageUtil.repair(tool, (int)amount);
            tool.updateStack(toolItem);
            stack.shrink(1);
            player.playSound(Sounds.SAW.getSound(), 1, 0.8f + 0.4f * player.level().getRandom().nextFloat());
          }
        }
        return true;
      }
    }
    return false;
  }
}
