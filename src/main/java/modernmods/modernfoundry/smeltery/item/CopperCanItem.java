package modernmods.modernfoundry.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.compat.neoforged.neoforge.registries.ForgeRegistries;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluid container holding 1 ingot of fluid
 */
public class CopperCanItem extends Item {
  private static final String TAG_FLUID = "fluid";
  private static final String TAG_FLUID_TAG = "fluid_tag";

  public CopperCanItem(Properties properties) {
    super(properties);
  }

  @Override
  public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance stack) {
    if (stack instanceof ItemStack itemStack && getFluid(itemStack) != Fluids.EMPTY) {
      return new ItemStackTemplate(this);
    }
    return null;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    List<Component> tooltip = new java.util.ArrayList<>();
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      CompoundTag fluidTag = getFluidTag(stack);
      MutableComponent text;
      if (fluidTag != null) {
        FluidStack displayFluid = TagUtil.createFluidStack(fluid, FluidValues.INGOT, fluidTag);
        text = displayFluid.getHoverName().plainCopy();
      } else {
        text = Component.translatable(fluid.getFluidType().getDescriptionId());
      }
      tooltip.add(Component.translatable(this.getDescriptionId() + ".contents", text).withStyle(ChatFormatting.GRAY));
      if (flag.isAdvanced()) {
        tooltip.add(Component.translatable(TankItem.FLUID_ID, Loadables.FLUID.getKey(fluid).toString()).withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
  
    tooltip.forEach(tooltipConsumer);
  }

  /** Removes the fluid from the given stack */
  public static void removeFluid(ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null) {
      nbt.remove(TAG_FLUID);
      nbt.remove(TAG_FLUID_TAG);
      TagUtil.setTag(stack, nbt);
    }
  }

  /** Sets the fluid on the given stack whether or not its valiid */
  private static void setFluidInternal(ItemStack stack, Identifier fluid, @Nullable CompoundTag fluidTag) {
    CompoundTag nbt = TagUtil.getOrCreateTag(stack);
    nbt.putString(TAG_FLUID, fluid.toString());
    if (fluidTag != null) {
      nbt.put(TAG_FLUID_TAG, fluidTag.copy());
    } else {
      nbt.remove(TAG_FLUID_TAG);
    }
    TagUtil.setTag(stack, nbt);
  }


  /** Sets the fluid on the given stack */
  @SuppressWarnings("deprecation")
  public static ItemStack setFluid(ItemStack stack, Identifier fluid, @Nullable CompoundTag fluidTag) {
    // if empty, try to remove the NBT, helps with recipes
    if (fluid.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
      removeFluid(stack);
    } else {
      setFluidInternal(stack, fluid, fluidTag);
    }
    return stack;
  }
  /** Sets the fluid on the given stack */
  @SuppressWarnings("deprecation")
  public static ItemStack setFluid(ItemStack stack, Fluid fluid, @Nullable CompoundTag fluidTag) {
    // if empty, try to remove the NBT, helps with recipes
    if (fluid == Fluids.EMPTY) {
      removeFluid(stack);
    } else {
      setFluidInternal(stack, BuiltInRegistries.FLUID.getKey(fluid), fluidTag);
    }
    return stack;
  }

  /** Sets the fluid on the given stack */
  public static ItemStack setFluid(ItemStack stack, FluidStack fluid) {
    return setFluid(stack, fluid.getFluid(), TagUtil.getTag(fluid));
  }

  /** Gets the fluid from the given stack */
  public static Fluid getFluid(ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null && nbt.contains(TAG_FLUID)) {
      Identifier location = Identifier.tryParse(nbt.getStringOr(TAG_FLUID, ""));
      if (location != null && ForgeRegistries.FLUIDS.containsKey(location)) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(location);
        if (fluid != null) {
          return fluid;
        }
      }
    }
    return Fluids.EMPTY;
  }

  /** Adds filled variants of the copper can to the given consumer */
  @SuppressWarnings("deprecation")
  public static void addFilledVariants(Consumer<ItemStack> output) {
    BuiltInRegistries.FLUID.listElements().filter(holder -> {
      Fluid fluid = holder.value();
      return fluid.isSource(fluid.defaultFluidState()) && !holder.is(TinkerTags.Fluids.HIDE_IN_CREATIVE_TANKS);
    }).forEachOrdered(holder -> {
      output.accept(CopperCanItem.setFluid(new ItemStack(TinkerSmeltery.copperCan), holder.key().identifier(), null));
    });
  }

  /** Gets the fluid NBT from the given stack */
  @Nullable
  public static CompoundTag getFluidTag(ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null && nbt.contains(TAG_FLUID_TAG)) {
      return nbt.getCompoundOrEmpty(TAG_FLUID_TAG);
    }
    return null;
  }

  /**
   * Gets a string variant name for the given stack
   * @param stack  Stack instance to check
   * @return  String variant name
   */
  public static String getSubtype(ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null) {
      return nbt.getStringOr(TAG_FLUID, "");
    }
    return "";
  }
}
