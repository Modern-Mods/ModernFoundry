package modernmods.modernfoundry.library.modifiers;

import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.Test;
import modernmods.modernfoundry.test.BaseMcTest;

import java.util.stream.Stream;

import modernmods.modernfoundry.library.modifiers.impl.ComposableModifier;

import static org.assertj.core.api.Assertions.assertThatCode;

class UpdateModifiersPacketTest extends BaseMcTest {
  private static final ResourceKey<Enchantment> ENCHANTMENT = ResourceKey.create(
    Registries.ENCHANTMENT,
    ResourceLocation.fromNamespaceAndPath("test", "network_enchantment")
  );
  private static final ModifierId MODIFIER = new ModifierId("test", "network_modifier");

  @Test
  void decodesEnchantmentMappingsFromConnectionRegistryBeforeClientWorldExists() {
    ComposableModifier modifier = ComposableModifier.builder().build();
    ((Modifier) modifier).setId(MODIFIER);
    FriendlyByteBuf buffer = registryBufferWithEnchantment();

    buffer.writeVarInt(1);
    buffer.writeUtf(MODIFIER.toString());
    ComposableModifier.LOADER.encode(buffer, modifier);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarInt(1);
    buffer.writeResourceLocation(ENCHANTMENT.location());
    buffer.writeResourceLocation(MODIFIER);
    buffer.writeVarInt(0);

    assertThatCode(() -> new UpdateModifiersPacket(buffer)).doesNotThrowAnyException();
  }

  private static FriendlyByteBuf registryBufferWithEnchantment() {
    MappedRegistry<Enchantment> enchantments = new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());
    Enchantment enchantment = new Enchantment(
      Component.literal("Network Test"),
      Enchantment.definition(
        HolderSet.<Item>empty(),
        1,
        1,
        Enchantment.constantCost(1),
        Enchantment.constantCost(1),
        1,
        EquipmentSlotGroup.ANY
      ),
      HolderSet.empty(),
      DataComponentMap.EMPTY
    );
    enchantments.register(ENCHANTMENT, enchantment, RegistrationInfo.BUILT_IN);

    RegistryAccess registries = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(
      RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries(),
      Stream.<RegistryAccess.RegistryEntry<?>>of(new RegistryAccess.RegistryEntry<>(Registries.ENCHANTMENT, enchantments))
    ));
    return new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
  }
}
