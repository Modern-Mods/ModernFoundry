package modernmods.modernfoundry.tables.block.entity.inventory;

import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import modernmods.mantle.recipe.container.ISingleStackContainer;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tables.block.entity.table.TinkerStationBlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;

import static modernmods.modernfoundry.tables.block.entity.table.TinkerStationBlockEntity.INPUT_SLOT;
import static modernmods.modernfoundry.tables.block.entity.table.TinkerStationBlockEntity.TINKER_SLOT;

public class TinkerStationContainerWrapper implements IMutableTinkerStationContainer {
  private final TinkerStationBlockEntity station;
  /** Cache of the material recipes found in each slot */
  private MaterialRecipe[] materials;
  /** Cache of whether each slot has been searched for a material */
  private boolean[] searchedMaterial;

  /** Cached tool instance to save lookup effort */
  @Nullable
  private LazyToolStack tool;

  private MaterialRecipe lastMaterialRecipe;
  @Nullable @Setter
  private Player player;

  /**
   * Creates a new wrapper instance for the station
   * @param station  Station instance
   */
  public TinkerStationContainerWrapper(TinkerStationBlockEntity station) {
    this.station = station;
    int count = station.getInputCount();
    this.materials = new MaterialRecipe[count];
    this.searchedMaterial = new boolean[count];
  }

  /**
   * Finds a material recipe for the given slot
   * @param stack  Stack in slot
   * @return  Material recipe found, or null if missing
   */
  @Nullable
  private MaterialRecipe findMaterialRecipe(ItemStack stack) {
    // must have world
    Level world = station.getLevel();
    if (world == null) {
      return null;
    }
    // try last recipe
    ISingleStackContainer inv = () -> stack;
    if (lastMaterialRecipe != null && lastMaterialRecipe.matches(inv, world)) {
      return lastMaterialRecipe;
    }
    // try to find a new recipe (side-aware: server RecipeManager, or the synced client recipe cache on the client)
    MaterialRecipe newRecipe = MaterialRecipe.getRecipe(inv, world);
    if (newRecipe != null) {
      lastMaterialRecipe = newRecipe;
      return lastMaterialRecipe;
    }
    // if none found, return null
    return null;
  }

  /**
   * Clears the cached inputs
   */
  public void refreshInput(int slot) {
    if (slot == TINKER_SLOT) {
      tool = null;
    } else if (slot >= INPUT_SLOT && slot < station.getInputCount() + INPUT_SLOT) {
      this.materials[slot - 1] = null;
      this.searchedMaterial[slot - 1] = false;
    }
  }

  /** Refreshes the size of this based on the size of the tinker station */
  public void resize() {
    int count = station.getInputCount();
    if (count != materials.length) {
      this.materials = new MaterialRecipe[count];
      this.searchedMaterial = new boolean[count];
    }
  }

  @Override
  public ItemStack getTinkerableStack() {
    return this.station.getItem(TINKER_SLOT);
  }

  // TODO: consider replacing getTinkerable and getTinkerableStack with this
  /** Gets the contents of the tool slot */
  public LazyToolStack getTool() {
    if (tool == null) {
      tool = LazyToolStack.from(getTinkerableStack());
    }
    return tool;
  }

  @Override
  public ToolStack getTinkerable() {
    return getTool().getTool();
  }

  @Override
  public ItemStack getInput(int index) {
    if (index < 0 || index >= station.getInputCount()) {
      return ItemStack.EMPTY;
    }
    return this.station.getItem(index + TinkerStationBlockEntity.INPUT_SLOT);
  }

  @Override
  public int getInputCount() {
    return station.getInputCount();
  }

  @Nullable
  @Override
  public MaterialRecipe getInputMaterial(int index) {
    if (index < 0 || index >= station.getInputCount()) {
      return null;
    }
    if (!searchedMaterial[index]) {
      materials[index] = findMaterialRecipe(getInput(index));
      searchedMaterial[index] = true;
    }
    return materials[index];
  }

  @Override
  public void setInput(int index, ItemStack stack) {
    if (index >= 0 && index < station.getInputCount()) {
      this.station.setItem(index + TinkerStationBlockEntity.INPUT_SLOT, stack);
    }
  }

  @Override
  public void giveItem(ItemStack stack) {
    if (player != null) {
      player.getInventory().placeItemBackInInventory(stack);
    }
  }
}
