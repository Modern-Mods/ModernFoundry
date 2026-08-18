package modernmods.modernfoundry.library.recipe.partbuilder;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.client.SafeClientAccess;
import modernmods.mantle.util.RegistryHelper;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.materials.definition.MaterialVariant;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipeCache;

import java.util.List;

/**
 * Part builder recipes that can show in JEI.
 * TODO 1.21: make this no longer extend {@link IPartBuilderRecipe}; copy the needed methods for JEI to this interface.
 */
public interface IDisplayPartBuilderRecipe extends IPartBuilderRecipe {
  /**
   * Gets the material variant required to craft this recipe.
   * TODO 1.21: make this return {@link modernmods.modernfoundry.library.materials.definition.MaterialVariantId}
   */
  MaterialVariant getMaterial();

  /** Gets a list of input material items for display in the material slot. */
  default List<ItemStack> getMaterialItems() {
    MaterialVariant material = getMaterial();
    if (material.isUnknown()) {
      return List.of();
    }
    return MaterialRecipeCache.getItems(material.getVariant());
  }

  /** Gets the recipe result for display (26.1.2 removed vanilla Recipe#getResultItem, so declared here for JEI). */
  ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider access);

  /** Gets a list of results. Should either be size 1, or size matching {@link #getMaterialItems()} */
  default List<ItemStack> getResultItems() {
    RegistryAccess access = SafeClientAccess.getRegistryAccess();
    return access == null ? List.of() : List.of(getResultItem(access));
  }

  /**
   * Gets a list of pattern items to display in the pattern slot
   * @return  Pattern items
   */
  default List<ItemStack> getPatternItems() {
    return RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.DEFAULT_PATTERNS).map(ItemStack::new).toList();
  }
}
