package modernmods.modernfoundry.smeltery.block.entity.module;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import modernmods.hilt.block.entity.HiltBlockEntity;
import modernmods.modernfoundry.test.BaseMcTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MeltingModuleTest extends BaseMcTest {
  private static final ResourceKey<Enchantment> TEST_ENCHANTMENT = ResourceKey.create(
    Registries.ENCHANTMENT,
    ResourceLocation.fromNamespaceAndPath("modernfoundry", "serialization_test")
  );

  @Test
  void providerAwareSerializationRoundTripsEnchantedStack() {
    HolderLookup.Provider registries = providerWithTestEnchantment();
    Holder<Enchantment> enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TEST_ENCHANTMENT);

    ItemStack expected = new ItemStack(Items.ENCHANTED_BOOK);
    ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
    enchantments.set(enchantment, 1);
    EnchantmentHelper.setEnchantments(expected, enchantments.toImmutable());

    MeltingModule module = newModule();
    module.setStack(expected);
    CompoundTag nbt = module.writeToTag(registries);

    MeltingModule restored = newModule();
    restored.readFromTag(nbt, registries);

    assertThat(ItemStack.matches(expected, restored.getStack())).isTrue();
  }

  private static MeltingModule newModule() {
    return new MeltingModule(mock(HiltBlockEntity.class), recipe -> false, (rate, amount) -> amount, -1);
  }

  private static HolderLookup.Provider providerWithTestEnchantment() {
    MappedRegistry<Enchantment> enchantments = new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());
    Enchantment enchantment = new Enchantment(
      Component.literal("Serialization Test"),
      Enchantment.definition(
        net.minecraft.core.HolderSet.<net.minecraft.world.item.Item>empty(),
        1,
        1,
        Enchantment.constantCost(1),
        Enchantment.constantCost(1),
        1,
        EquipmentSlotGroup.ANY
      ),
      net.minecraft.core.HolderSet.empty(),
      DataComponentMap.EMPTY
    );
    enchantments.register(TEST_ENCHANTMENT, enchantment, RegistrationInfo.BUILT_IN);

    return new RegistryAccess.ImmutableRegistryAccess(Stream.concat(
      RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries(),
      Stream.<RegistryAccess.RegistryEntry<?>>of(new RegistryAccess.RegistryEntry<>(Registries.ENCHANTMENT, enchantments))
    ));
  }
}
