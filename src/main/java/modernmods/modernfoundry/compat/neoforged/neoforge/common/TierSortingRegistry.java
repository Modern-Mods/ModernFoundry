package modernmods.modernfoundry.compat.neoforged.neoforge.common;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compatibility replacement for the removed Forge {@code TierSortingRegistry}.
 * <p>
 * In 26.1 the {@code Tier}/{@code Tiers} types were replaced by {@link ToolMaterial}, and mining correctness became
 * tag-based ({@link ToolMaterial#incorrectBlocksForDrops()}) rather than a numeric harvest level. Tinkers still models a
 * harvest "level" ordering (wood &lt; gold &lt; stone &lt; copper &lt; iron &lt; diamond &lt; netherite), so this class keeps a sorted
 * list of the vanilla tool materials plus name lookups, and defers "can mine" checks to the block tag.
 */
public final class TierSortingRegistry {
  /** Ordered list of vanilla tool materials from weakest to strongest harvest level */
  private static final List<ToolMaterial> SORTED_TIERS = List.of(
    ToolMaterial.WOOD, ToolMaterial.GOLD, ToolMaterial.STONE, ToolMaterial.COPPER,
    ToolMaterial.IRON, ToolMaterial.DIAMOND, ToolMaterial.NETHERITE);

  /** Bidirectional mapping between vanilla ids and tool materials */
  private static final Map<Identifier, ToolMaterial> BY_NAME = new LinkedHashMap<>();
  private static final Map<ToolMaterial, Identifier> BY_TIER = new LinkedHashMap<>();
  static {
    register("wood", ToolMaterial.WOOD);
    register("gold", ToolMaterial.GOLD);
    register("stone", ToolMaterial.STONE);
    register("copper", ToolMaterial.COPPER);
    register("iron", ToolMaterial.IRON);
    register("diamond", ToolMaterial.DIAMOND);
    register("netherite", ToolMaterial.NETHERITE);
  }

  private static void register(String path, ToolMaterial material) {
    Identifier id = Identifier.withDefaultNamespace(path);
    BY_NAME.put(id, material);
    BY_TIER.put(material, id);
  }

  private TierSortingRegistry() {}

  /** Gets the list of tool materials sorted from weakest to strongest harvest level */
  public static List<ToolMaterial> getSortedTiers() {
    return SORTED_TIERS;
  }

  /** Gets the registered id for the given tool material, or null if it is not a known vanilla material */
  @Nullable
  public static Identifier getName(ToolMaterial material) {
    return BY_TIER.get(material);
  }

  /** Gets the tool material for the given id, accepting the "wooden"/"golden" aliases; null if unknown */
  @Nullable
  public static ToolMaterial byName(Identifier name) {
    ToolMaterial material = BY_NAME.get(name);
    if (material != null) {
      return material;
    }
    // accept the material-tag style aliases used by some datapacks
    if (name.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
      return switch (name.getPath()) {
        case "wooden" -> ToolMaterial.WOOD;
        case "golden" -> ToolMaterial.GOLD;
        default -> null;
      };
    }
    return null;
  }

  /** Checks whether the given tool material can correctly harvest the given block (tag-based in 26.1) */
  public static boolean isCorrectTierForDrops(ToolMaterial material, BlockState state) {
    return !state.is(material.incorrectBlocksForDrops());
  }
}
