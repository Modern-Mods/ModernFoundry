package modernmods.modernfoundry.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import modernmods.modernfoundry.library.fluid.SimpleFluidResourceTank;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.fluid.FluidTransferHelper;
import modernmods.mantle.fluid.tooltip.FluidTooltipHandler;
import modernmods.mantle.fluid.transfer.FluidContainerTransferManager;
import modernmods.mantle.fluid.transfer.IFluidContainerTransfer.TransferDirection;
import modernmods.mantle.fluid.transfer.IFluidContainerTransfer.TransferResult;
import modernmods.mantle.item.BlockTooltipItem;
import modernmods.mantle.registration.object.EnumObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.utils.NBTTags;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.block.component.SearedTankBlock.TankType;
import modernmods.modernfoundry.smeltery.block.entity.component.TankBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TankItem extends BlockTooltipItem {
  public static final String FLUID_ID = TConstruct.makeTranslationKey("item", "tank.fluid_id");
  private static final Predicate<FluidStack> NO_FILL = FluidStack::isEmpty;
  private final boolean limitStackSize;
  public TankItem(Block blockIn, Properties builder, boolean limitStackSize) {
    super(blockIn, builder);
    this.limitStackSize = limitStackSize;
  }

  /** Checks if the tank item is filled */
  private static boolean isFilled(ItemStack stack) {
    // has a container if not empty
    CompoundTag nbt = TagUtil.getTag(stack);
    return nbt != null && nbt.contains(NBTTags.TANK);
  }

  @Override
  public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance stack) {
    return stack instanceof ItemStack itemStack && isFilled(itemStack) ? new ItemStackTemplate(this) : null;
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    if (!limitStackSize) {
      return super.getMaxStackSize(stack);
    }
    return isFilled(stack) ? 16: 64;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    List<Component> tooltip = new java.util.ArrayList<>();
    if (TagUtil.hasTag(stack)) {
      SimpleFluidResourceTank tank = getTank(stack, 1);
      if (tank.getFluidAmount() > 0) {
        FluidStack fluid = tank.getFluid();
        tooltip.add(fluid.getHoverName().plainCopy().withStyle(ChatFormatting.GRAY));
        if (flag.isAdvanced()) {
          tooltip.add(Component.translatable(FLUID_ID, Loadables.FLUID.getKey(fluid.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        FluidTooltipHandler.appendMaterial(fluid, tooltip);
      }
    }
    else {
      super.appendHoverText(stack, context, tooltipDisplay, tooltip::add, flag);
    }

    tooltip.forEach(tooltipConsumer);
  }

  /** Checks if the given stack has fluid transfer */
  public static boolean mayHaveFluid(ItemStack stack) {
    return FluidContainerTransferManager.INSTANCE.mayHaveTransfer(stack)
      || (!stack.isEmpty() && Capabilities.Fluid.ITEM.getCapability(stack, net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack)) != null);
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack held, Slot slot, ClickAction action, Player player) {
    // take over right click, assuming the target has an item. If not, then we want to place 1 item in the slot
    if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
      ItemStack slotStack = slot.getItem();
      // if it's the same item, we might want to transfer fluid or just move 1 item; overrideOtherStackedOnMe will handle deciding which to take
      if (!slotStack.isEmpty() && held.getItem() != slotStack.getItem() && mayHaveFluid(slotStack)) {
        // target must be stack size 1, if not then it's not safe to modify it
        if (slotStack.getCount() == 1) {
          // transfer fluid - but we work with just 1 tank at a time instead of trying to transfer the whole stack
          SimpleFluidResourceTank tank = getTank(held, 1);
          TransferResult result = FluidTransferHelper.interactWithStack(tank, slotStack, TransferDirection.REVERSE);
          // update held tank and slot item if something changed
          if (result != null) {
            // play sound
            if (player.level().isClientSide()) {
              player.playSound(result.getSound());
            }
            // update stack
            slot.set(FluidTransferHelper.getOrTransferFilled(player, slotStack, result.stack()));
            // deal with remainder
            if (held.getCount() == 1) {
              setTank(held, tank);
            } else {
              // if we have multiple, toss the update anywhere
              ItemStack split = held.split(1);
              setTank(split, tank);
              if (!player.getInventory().add(split)) {
                player.drop(split, false);
              }
            }
          }
        } else {
          // we don't try filling items with a larger stack size as our transfer logic does not support that
          // however, supposing that item accepts it in their stack on me logic, let them respond
          // this won't run twice as we will be returning true regardless
          if (slotStack.isItemEnabled(player.level().enabledFeatures())) {
            AbstractContainerMenu menu = player.containerMenu;
            slotStack.overrideOtherStackedOnMe(held, slot, action, player, new SlotAccess() {
              @Override
              public ItemStack get() {
                return menu.getCarried();
              }

              @Override
              public boolean set(ItemStack stack) {
                menu.setCarried(stack);
                return true;
              }
            });
          }
        }
        return true;
      }
    }
    return false;
  }

  /** Updates the item the player is holding from the old instance */
  public static void updateHeldItem(Player player, ItemStack held, ItemStack result) {
    if (player.containerMenu.getCarried() == held) {
      player.containerMenu.setCarried(FluidTransferHelper.getOrTransferFilled(player, held, result));
    } else if (!player.getInventory().add(result)) {
      player.drop(result, false);
    }
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess pAccess) {
    // take over right click, unless there is no held item (we still want split stack support)
    if (action == ClickAction.SECONDARY && slot.allowModification(player) && !held.isEmpty() && mayHaveFluid(held)) {
      // we can safely modify tanks of size larger than 1,
      // though our fluid transfer logic does not handle well transferring between two tanks with no 1mb increments
      if (stack.getCount() == 1 || held.getItem() instanceof TankItem) {
        // transfer the fluid
        SimpleFluidResourceTank tank = getTank(stack);
        // if both tanks are empty, just do standard stack operations; makes it nice and easy to move just 1 item at a time
        if (tank.isEmpty() && ItemStack.isSameItemSameComponents(stack, held)) {
          return false;
        }
        TransferResult result = FluidTransferHelper.interactWithStack(tank, held, TransferDirection.AUTO);
        if (result != null) {
          // play sound
          if (player.level().isClientSide()) {
            player.playSound(result.getSound());
          }
          // update tank
          setTank(stack, tank);
          // update held item, assuming its actually held
          updateHeldItem(player, held, result.stack());
        }
      }
      return true;
    }
    return false;
  }

  /** Removes the tank from the given stack */
  private static void removeTank(ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null) {
      nbt.remove(NBTTags.TANK);
      TagUtil.setTag(stack, nbt);
    }
  }

  /** Writes a fluid stack to the legacy tank item tag shape. */
  public static CompoundTag writeFluid(FluidStack fluid) {
    CompoundTag tag = new CompoundTag();
    if (!fluid.isEmpty()) {
      tag.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
      tag.putInt("Amount", fluid.getAmount());
    }
    return tag;
  }

  /** Reads a fluid stack from the legacy tank item tag shape. */
  public static FluidStack readFluid(CompoundTag tag) {
    Identifier fluidName = Identifier.tryParse(tag.getStringOr("FluidName", ""));
    int amount = tag.getIntOr("Amount", 0);
    if (fluidName == null || amount <= 0) {
      return FluidStack.EMPTY;
    }
    return BuiltInRegistries.FLUID.getOptional(fluidName).map(fluid -> new FluidStack(fluid, amount)).orElse(FluidStack.EMPTY);
  }

  /** Writes a tank to the legacy tank item tag shape. */
  public static CompoundTag writeTank(IFluidTank tank) {
    return writeFluid(tank.getFluid());
  }

  /** Reads a tank from the legacy tank item tag shape. */
  public static void readTank(SimpleFluidResourceTank tank, CompoundTag tag) {
    tank.setFluid(readFluid(tag));
  }

  /**
   * Sets the tank to the given stack
   * @param stack  Stack
   * @param tank   Tank instance
   * @return  Stack with tank
   */
  public static ItemStack setTank(ItemStack stack, IFluidTank tank) {
    if (tank.getFluid().isEmpty()) {
      removeTank(stack);
    } else {
      CompoundTag nbt = TagUtil.getOrCreateTag(stack);
      nbt.put(NBTTags.TANK, writeTank(tank));
      TagUtil.setTag(stack, nbt);
    }
    return stack;
  }

  /**
   * Sets the tank to the given stack
   * @param stack  Stack
   * @param fluid  Fluid
   * @return  Stack with tank
   */
  public static ItemStack setTank(ItemStack stack, FluidStack fluid) {
    if (fluid.isEmpty()) {
      removeTank(stack);
    } else {
      CompoundTag nbt = TagUtil.getOrCreateTag(stack);
      nbt.put(NBTTags.TANK, writeFluid(fluid));
      TagUtil.setTag(stack, nbt);
    }
    return stack;
  }

  /** Creates a stack with the given fluid and amount, not validated. */
  private static ItemStack setTank(ItemLike item, Identifier fluid, int amount) {
    CompoundTag tag = new CompoundTag();
    tag.putString("FluidName", fluid.toString());
    tag.putInt("Amount", amount);
    ItemStack stack = new ItemStack(item);
    CompoundTag nbt = new CompoundTag();
    nbt.put(NBTTags.TANK, tag);
    TagUtil.setTag(stack, nbt);
    return stack;
  }

  /**
   * Gets the tank for the given stack, scaled by the stack size.
   * @param stack  Tank stack
   * @return  Tank stored in the stack
   */
  public SimpleFluidResourceTank getTank(ItemStack stack) {
    int count = stack.getCount();
    SimpleFluidResourceTank tank = getTank(stack, count);
    // disallow filling if the current size is larger than 16
    if (limitStackSize && count > 16) {
      tank.setValidator(NO_FILL);
    }
    return tank;
  }

  /**
   * Gets the tank for the given stack
   * @param stack  Tank stack
   * @param scale  Number of tanks in a stack, being filled or drained together.
   * @return  Tank stored in the stack
   */
  public static SimpleFluidResourceTank getTank(ItemStack stack, int scale) {
    SimpleFluidResourceTank tank = ScaledFluidTank.create(TankBlockEntity.getCapacity(stack.getItem()), scale);
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null) {
      readTank(tank, nbt.getCompoundOrEmpty(NBTTags.TANK));
    }
    return tank;
  }

  /**
   * Gets a string variant name for the given stack
   * @param stack  Stack instance to check
   * @return  String variant name
   */
  public static String getSubtype(ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null && nbt.contains(NBTTags.TANK)) {
      return nbt.getCompoundOrEmpty(NBTTags.TANK).getStringOr("FluidName", "");
    }
    return "";
  }

  /** Adds filled variants of all standard tank items to the given consumer */
  @SuppressWarnings("deprecation")
  public static void addFilledVariants(Consumer<ItemStack> output) {
    BuiltInRegistries.FLUID.listElements().filter(holder -> {
      Fluid fluid = holder.value();
      return fluid.isSource(fluid.defaultFluidState()) && !holder.is(TinkerTags.Fluids.HIDE_IN_CREATIVE_TANKS);
    }).forEachOrdered(holder -> {
      // use an ingot variety for metals
      TankType tank, gauge;
      if (holder.is(TinkerTags.Fluids.METAL_TOOLTIPS)) {
        tank = TankType.INGOT_TANK;
        gauge = TankType.INGOT_GAUGE;
      } else {
        tank = TankType.FUEL_TANK;
        gauge = TankType.FUEL_GAUGE;
      }
      Identifier fluidName = holder.key().identifier();
      output.accept(setTank(TinkerSmeltery.searedLantern, fluidName, FluidValues.LANTERN_CAPACITY));
      output.accept(fillTank(TinkerSmeltery.searedTank, tank, fluidName));
      output.accept(fillTank(TinkerSmeltery.searedTank, gauge, fluidName));
      output.accept(setTank(TinkerSmeltery.scorchedLantern, fluidName, FluidValues.LANTERN_CAPACITY));
      output.accept(fillTank(TinkerSmeltery.scorchedTank, tank, fluidName));
      output.accept(fillTank(TinkerSmeltery.scorchedTank, gauge, fluidName));
    });
  }

  /** Fills a tank stack with the given fluid */
  public static ItemStack fillTank(EnumObject<TankType,? extends ItemLike> tank, TankType type, Fluid fluid) {
    return setTank(new ItemStack(tank.get(type)), new FluidStack(fluid, type.getCapacity()));
  }

  /** Fills a tank stack with the given fluid */
  public static ItemStack fillTank(EnumObject<TankType,? extends ItemLike> tank, TankType type, Identifier fluid) {
    return setTank(tank.get(type), fluid, type.getCapacity());
  }
}
