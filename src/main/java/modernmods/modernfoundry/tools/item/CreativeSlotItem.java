package modernmods.modernfoundry.tools.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import modernmods.mantle.command.MantleCommand;
import modernmods.mantle.fluid.FluidTransferHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.SlotType;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modifiers.slotless.CreativeSlotModifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CreativeSlotItem extends Item {
  private static final String NBT_KEY = "slot";
  private static final String TOOLTIP = TConstruct.makeTranslationKey("item", "creative_slot.tooltip");
  private static final Component TOOLTIP_MISSING = TConstruct.makeTranslation("item", "creative_slot.missing").withStyle(ChatFormatting.RED);
  private static final Component CREATIVE_ONLY = TConstruct.makeTranslation("item", "creative_slot.only").withStyle(ChatFormatting.RED);

  public CreativeSlotItem(Properties properties) {
    super(properties);
  }

  /** Gets the value of the slot tag from the given stack */
  @Nullable
  public static SlotType getSlot(ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null && nbt.contains(NBT_KEY)) {
      return SlotType.getIfPresent(nbt.getStringOr(NBT_KEY, ""));
    }
    return null;
  }

  /** Makes an item stack with the given slot type */
  public static ItemStack withSlot(ItemStack stack, SlotType type) {
    CompoundTag tag = TagUtil.getOrCreateTag(stack);
    tag.putString(NBT_KEY, type.getName());
    TagUtil.setTag(stack, tag);
    return stack;
  }

  @Override
  public Component getName(ItemStack stack) {
    // 26.1.2 removed Item#getDescriptionId(ItemStack); override getName to provide the slot-specific name
    SlotType slot = getSlot(stack);
    String originalKey = getDescriptionId();
    if (slot != null) {
      String betterKey = originalKey + "." + slot.getName();
      if (Util.canTranslate(betterKey)) {
        return Component.translatable(betterKey);
      }
    }
    return Component.translatable(originalKey);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    List<Component> tooltip = new java.util.ArrayList<>();
    SlotType slot = getSlot(stack);
    if (slot != null) {
      tooltip.add(Component.translatable(TOOLTIP, slot.getDisplayName()).withStyle(ChatFormatting.GRAY));
    } else {
      tooltip.add(TOOLTIP_MISSING);
    }
  
    tooltip.forEach(tooltipConsumer);
  }

  /** Adds all variants of this slot item to the creative tab */
  public void addVariants(Consumer<ItemStack> items) {
    Collection<SlotType> allTypes = SlotType.getAllSlotTypes();
    if (allTypes.isEmpty()) {
      items.accept(new ItemStack(this));
    } else {
      for (SlotType type : allTypes) {
        items.accept(withSlot(new ItemStack(this), type));
      }
    }
  }

  /** Checks if the given player may apply this item */
  public static boolean canApply(Player player) {
    return player.isCreative() || (Config.COMMON.quickApplyToolModifiersSurvival.get() && MantleCommand.PERMISSION_GAME_COMMANDS.check(player.permissions()));
  }

  /** Common logic between two stack methods */
  private static boolean handleStackOn(ItemStack stack, ItemStack toolItem, Player player, int amount) {
    SlotType slotType = getSlot(stack);
    if (slotType != null && !toolItem.isEmpty() && toolItem.is(TinkerTags.Items.MODIFIABLE)) {
      if (!player.level().isClientSide() || (player.isCreative() && player.containerMenu.getType() == null)) {
        if (canApply(player)) {
          ToolStack tool = ToolStack.from(toolItem);
          // do nothing if the tool already has 0 slots and we are removing
          if (tool.getFreeSlots(slotType) + amount < 0) {
            return true;
          }

          // find the tool data
          ModDataNBT persistentData = tool.getPersistentData();
          CompoundTag slots;
          if (persistentData.contains(CreativeSlotModifier.KEY_SLOTS)) {
            slots = persistentData.getCompound(CreativeSlotModifier.KEY_SLOTS);
          } else {
            slots = new CompoundTag();
            persistentData.put(CreativeSlotModifier.KEY_SLOTS, slots);
          }

          // add the slot
          String name = slotType.getName();
          int updated = slots.getIntOr(name, 0) + amount;
          if (updated == 0) {
            slots.remove(name);
          } else {
            slots.putInt(name, updated);
          }

          // if no slot remain in the creative modifier, remove it
          ModifierId creative = TinkerModifiers.creativeSlot.getId();
          int currentLevel = tool.getModifierLevel(creative);
          if (slots.isEmpty()) {
            // if no slots exist anymore, remove the creative modifier
            persistentData.remove(CreativeSlotModifier.KEY_SLOTS);
            tool.removeModifier(creative, currentLevel);
          } else if (currentLevel == 0) {
            // add creative modifier if needed
            tool.addModifier(creative, 1);
          } else {
            // neither add or removing modifier, just build it
            tool.rebuildStats();
          }
          if (amount > 0) {
            FluidTransferHelper.playUISound(player, SoundEvents.ENCHANTMENT_TABLE_USE);
          } else {
            FluidTransferHelper.playUISound(player, SoundEvents.GRINDSTONE_USE);
          }
        } else if (!player.isCreative()) {
          player.sendSystemMessage(CREATIVE_ONLY);
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
    if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
      // click tool with item - add slot
      return handleStackOn(stack, slot.getItem(), player, stack.getCount());
    }
    return false;
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack toolItem, Slot slot, ClickAction action, Player player, SlotAccess access) {
    if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
      // click item with tool - remove slot
      return handleStackOn(stack, toolItem, player, -stack.getCount());
    }
    return false;
  }
}
