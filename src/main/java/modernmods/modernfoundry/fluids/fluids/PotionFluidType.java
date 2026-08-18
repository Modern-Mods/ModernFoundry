package modernmods.modernfoundry.fluids.fluids;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import modernmods.mantle.recipe.helper.FluidOutput;
import modernmods.modernfoundry.fluids.TinkerFluids;
import modernmods.modernfoundry.library.utils.TagUtil;

import javax.annotation.Nullable;
import java.util.Objects;

public class PotionFluidType extends FluidType {
  public PotionFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public String getDescriptionId(FluidStack stack) {
    return "item.minecraft.potion.effect." + PotionUtils.getPotion(TagUtil.getTag(stack)).value().name();
  }

  @Override
  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    TagUtil.setTag(itemStack, TagUtil.getTag(fluidStack));
    return itemStack;
  }

  // 26.1: FluidType#initializeClient and the IClientFluidTypeExtensions#getTintColor(FluidStack) overload were both
  // removed. The per-stack potion tint now lives in PotionFluidTintSource (a FluidTintSource), wired to the potion
  // fluid model in FluidClientEvents#registerFluidModels.

  /** Creates the potion tag */
  private static CompoundTag potionTag(Identifier location) {
    CompoundTag tag = new CompoundTag();
    tag.putString("Potion", location.toString());
    return tag;
  }

  /** Creates a potion fluid stack with legacy potion custom data. */
  private static FluidStack potionFluid(@Nullable CompoundTag tag, int size) {
    FluidStack stack = new FluidStack(TinkerFluids.potion.get(), size);
    TagUtil.setTag(stack, tag);
    return stack;
  }

  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(ResourceKey<Potion> potion, int size) {
    CompoundTag tag = null;
    if (!potion.identifier().equals(Potions.WATER.unwrapKey().orElseThrow().identifier())) {
      tag = potionTag(potion.identifier());
    }
    return potionFluid(tag, size);
  }

  /** Creates a fluid stack for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidStack potionFluid(Potion potion, int size) {
    CompoundTag tag = null;
    Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
    if (!holder.is(Potions.WATER)) {
      tag = potionTag(BuiltInRegistries.POTION.getKey(potion));
    }
    return potionFluid(tag, size);
  }

  /** Creates a fluid output for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidOutput potionResult(Potion potion, int size) {
    CompoundTag tag = null;
    Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
    if (!holder.is(Potions.WATER)) {
      tag = potionTag(BuiltInRegistries.POTION.getKey(potion));
    }
    return FluidOutput.fromTag(Objects.requireNonNull(TinkerFluids.potion.getCommonTag()), size, tag);
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(ResourceKey<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (!potion.identifier().equals(Potions.WATER.unwrapKey().orElseThrow().identifier())) {
      TagUtil.setTag(stack, potionTag(potion.identifier()));
      // also bind the vanilla potion component so the item model's minecraft:potion tint colors the bucket per-potion
      BuiltInRegistries.POTION.get(potion).ifPresent(holder ->
        stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new net.minecraft.world.item.alchemy.PotionContents(holder)));
    }
    return stack;
  }

  /** Creates a potion bucket for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static ItemStack potionBucket(Potion potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
    if (!holder.is(Potions.WATER)) {
      TagUtil.setTag(stack, potionTag(BuiltInRegistries.POTION.getKey(potion)));
      // also bind the vanilla potion component so the item model's minecraft:potion tint colors the bucket per-potion
      stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new net.minecraft.world.item.alchemy.PotionContents(holder));
    }
    return stack;
  }
}
