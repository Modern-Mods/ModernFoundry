package modernmods.modernfoundry.gadgets.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

/** Shared variant state for the legacy slime gadgets. */
public final class SlimeGadgetDataComponents {
  private SlimeGadgetDataComponents() {}

  public static final DataComponentType<Integer> SLIME_TYPE = DataComponentType.<Integer>builder()
    .persistent(Codec.INT)
    .networkSynchronized(ByteBufCodecs.INT)
    .build();

  public static int getType(ItemStack stack) {
    return Math.max(0, Math.min(5, stack.getOrDefault(SLIME_TYPE, 0)));
  }

  public static String getTypeName(ItemStack stack) {
    return switch (getType(stack)) {
      case 1 -> "blue";
      case 2 -> "purple";
      case 3 -> "blood";
      case 4 -> "magma";
      case 5 -> "pink";
      default -> "green";
    };
  }

  public static int getColor(ItemStack stack) {
    return switch (getType(stack)) {
      case 1 -> 0x74c5c8;
      case 2 -> 0xcc68ff;
      case 3 -> 0xb80000;
      case 4 -> 0xffab49;
      case 5 -> 0xbc9eb4;
      default -> 0x69bc5e;
    };
  }
}
