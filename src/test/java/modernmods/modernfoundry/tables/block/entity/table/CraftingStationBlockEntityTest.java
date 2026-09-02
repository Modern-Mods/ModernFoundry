package modernmods.modernfoundry.tables.block.entity.table;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import modernmods.hilt.block.entity.InventoryBlockEntity;
import modernmods.modernfoundry.tables.block.entity.inventory.CraftingContainerWrapper;
import modernmods.modernfoundry.tables.block.entity.inventory.LazyResultContainer;
import modernmods.modernfoundry.test.BaseMcTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CraftingStationBlockEntityTest extends BaseMcTest {
  @Test
  void consumesIngredientsInTheirPositionedGridSlots() throws ReflectiveOperationException {
    CraftingStationBlockEntity station = mock(CraftingStationBlockEntity.class, CALLS_REAL_METHODS);
    NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
    setField(InventoryBlockEntity.class, station, "inventory", inventory);
    CraftingContainerWrapper craftingInventory = new CraftingContainerWrapper(station, 3, 3);
    setField(CraftingStationBlockEntity.class, station, "craftingInventory", craftingInventory);
    setField(CraftingStationBlockEntity.class, station, "craftingResult", new LazyResultContainer(station));

    Level level = mock(Level.class);
    station.setLevel(level);
    Player player = mock(Player.class);
    CraftingRecipe recipe = mock(CraftingRecipe.class);
    when(recipe.isSpecial()).thenReturn(true);
    when(recipe.getRemainingItems(any(CraftingInput.class)))
      .thenReturn(NonNullList.withSize(2, ItemStack.EMPTY));
    setField(CraftingStationBlockEntity.class, station, "lastRecipe", new RecipeHolder<>(
      ResourceLocation.fromNamespaceAndPath("modernfoundry", "test"), recipe));

    inventory.set(1, new ItemStack(Items.OAK_PLANKS));
    inventory.set(4, new ItemStack(Items.OAK_PLANKS));
    CraftingInput.Positioned positionedInput = CraftingInput.ofPositioned(3, 3, craftingInventory.getItems());
    assertThat(station.getLevel()).isSameAs(level);
    assertThat(positionedInput.input().width()).isEqualTo(1);
    assertThat(positionedInput.input().height()).isEqualTo(2);
    assertThat(positionedInput.left()).isEqualTo(1);
    assertThat(positionedInput.top()).isEqualTo(0);
    station.takeResult(player, new ItemStack(Items.STICK, 4), 1);

    assertThat(station.getItem(1).isEmpty()).isTrue();
    assertThat(station.getItem(4).isEmpty()).isTrue();
    assertThat(station.getItem(0).isEmpty()).isTrue();
  }

  private static void setField(Class<?> declaringClass, Object target, String name, Object value)
    throws ReflectiveOperationException {
    Field field = declaringClass.getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }
}
