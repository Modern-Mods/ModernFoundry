package modernmods.modernfoundry.tools.yoyo;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class YoyoDataComponents {
  private YoyoDataComponents() {}

  public static final DataComponentType<Boolean> ATTACK = DataComponentType.<Boolean>builder()
    .persistent(Codec.BOOL)
    .networkSynchronized(ByteBufCodecs.BOOL)
    .build();
  public static final DataComponentType<EnchantmentsState> ENCHANTMENTS = DataComponentType.<EnchantmentsState>builder()
    .persistent(EnchantmentsState.CODEC)
    .networkSynchronized(EnchantmentsState.STREAM_CODEC)
    .build();

}
