package modernmods.modernfoundry.library.utils;

import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ToolMaterial;
import modernmods.modernfoundry.compat.neoforged.neoforge.common.TierSortingRegistry;
import modernmods.mantle.client.ResourceColorManager;
import modernmods.mantle.data.listener.ISafeManagerReloadListener;
import modernmods.modernfoundry.TConstruct;

import java.util.List;
import java.util.Map;

/**
 * Harvest level display names
 */
public class HarvestTiers {
  private HarvestTiers() {}

  /** Cache of name for each tier */
  private static final Map<ToolMaterial, Component> harvestLevelNames = Maps.newHashMap();
  /** Listener to clear name cache so we get new colors */
  public static final ISafeManagerReloadListener RELOAD_LISTENER = manager -> harvestLevelNames.clear();

  /** Makes a translation key for the given name */
  private static MutableComponent makeLevelKey(ToolMaterial tier) {
    String key = Util.makeTranslationKey("harvest_tier", TierSortingRegistry.getName(tier));
    TextColor color = ResourceColorManager.getTextColor(key);
    return TConstruct.makeTranslation("stat", key).withStyle(style -> style.withColor(color));
  }

  /**
   * Gets the harvest level name for the given tool material
   * @param tier  Tool material
   * @return  Level name
   */
  public static Component getName(ToolMaterial tier) {
    return harvestLevelNames.computeIfAbsent(tier, n ->  makeLevelKey(tier));
  }

  /** Gets the larger of two tiers */
  public static ToolMaterial max(ToolMaterial a, ToolMaterial b) {
    List<ToolMaterial> sorted = TierSortingRegistry.getSortedTiers();
    // note indexOf returns -1 if the tier is missing, so the larger of an unsorted tier and a sorted one is the sorted one
    if (sorted.indexOf(b) > sorted.indexOf(a)) {
      return b;
    }
    return a;
  }

  /** Gets the smaller of two tiers */
  public static ToolMaterial min(ToolMaterial a, ToolMaterial b) {
    List<ToolMaterial> sorted = TierSortingRegistry.getSortedTiers();
    // note indexOf returns -1 if the tier is missing, so the smaller of an unsorted tier and a sorted one is the unsorted one
    if (sorted.indexOf(b) < sorted.indexOf(a)) {
      return b;
    }
    return a;
  }

  /** Gets the smallest tier in the sorting registry */
  public static ToolMaterial minTier() {
    List<ToolMaterial> sortedTiers = TierSortingRegistry.getSortedTiers();
    if (sortedTiers.isEmpty()) {
      TConstruct.LOG.error("No sorted tiers exist, this should not happen");
      return ToolMaterial.WOOD;
    }
    return sortedTiers.get(0);
  }
}
