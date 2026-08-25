package modernmods.modernfoundry.smeltery.network;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import modernmods.modernfoundry.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmelteryTankUpdatePacketTest extends BaseMcTest {
  @Test
  void packetSnapshotsFluidContentsBeforeEncoding() {
    FluidStack water = new FluidStack(Fluids.WATER, 1000);
    List<FluidStack> fluids = new ArrayList<>(List.of(water));
    SmelteryTankUpdatePacket packet = new SmelteryTankUpdatePacket(BlockPos.ZERO, fluids);

    water.setAmount(250);
    fluids.clear();

    RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
      Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    packet.encode(buffer);

    assertThat(buffer.readBlockPos()).isEqualTo(BlockPos.ZERO);
    assertThat(buffer.readVarInt()).isEqualTo(1);
    FluidStack encoded = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    assertThat(encoded.getFluid()).isEqualTo(Fluids.WATER);
    assertThat(encoded.getAmount()).isEqualTo(1000);
  }
}
