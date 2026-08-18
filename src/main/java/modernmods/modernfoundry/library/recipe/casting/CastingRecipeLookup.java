package modernmods.modernfoundry.library.recipe.casting;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.recipe.data.ItemNameOutput;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.util.RegistryHelper;
import modernmods.modernfoundry.common.recipe.RecipeCacheInvalidator;
import modernmods.modernfoundry.common.recipe.RecipeCacheInvalidator.DuelSidedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Lookup for items which are castable and other relevant information.
 */
public class CastingRecipeLookup {
  /** Set of all castable items */
  private static final Map<Item,Boolean> CASTABLE_ITEMS = new HashMap<>();
  /** List of tags that represent castable items */
  private static final List<TagKey<Item>> CASTABLE_TAGS = new ArrayList<>();
  /** Function to evaluate whether something is castable */
  private static final Function<Item,Boolean> COMPUTE_CASTABLE = item -> {
    for (TagKey<Item> tag : CASTABLE_TAGS) {
      if (RegistryHelper.contains(tag, item)) {
        return true;
      }
    }
    return false;
  };
  /** Cache invalidator */
  private static final DuelSidedListener INVALIDATOR = RecipeCacheInvalidator.addDuelSidedListener(() -> {
    CASTABLE_ITEMS.clear();
    CASTABLE_TAGS.clear();
  });

  private CastingRecipeLookup() {}

  /** Marks the given item as castable */
  public static void registerCastable(ItemLike item) {
    INVALIDATOR.checkClear();
    CASTABLE_ITEMS.put(item.asItem(), true);
  }

  /** Marks the given tag as castable */
  public static void registerCastable(TagKey<Item> tag) {
    INVALIDATOR.checkClear();
    CASTABLE_TAGS.add(tag);
  }

  /** Marks the given item output as castable */
  public static void registerCastable(ItemOutput output) {
    TagKey<Item> tag = output.getTag();
    if (tag != null) {
      registerCastable(tag);
    // item name output fails here, but that only happens at datagen so just ignore that
    } else if (!output.isEmpty() && output.getClass() != ItemNameOutput.class) {
      registerCastable(output.getItem());
    }
  }

  /** Checks if the given item is castable */
  public static boolean isCastable(ItemLike item) {
    return CASTABLE_ITEMS.computeIfAbsent(item.asItem(), COMPUTE_CASTABLE);
  }
}
