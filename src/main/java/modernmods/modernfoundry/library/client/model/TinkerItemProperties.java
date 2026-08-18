package modernmods.modernfoundry.library.client.model;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * Properties for tinker tools.
 * <p>
 * 26.1 removed the {@code ItemProperties}/{@code ItemPropertyFunction} predicate-override system (the old
 * {@code overrides} + {@code predicate} JSON). The broken/charging/charge/cast/ammo states that used to be
 * registered here are now data-driven through the new item model system (range_dispatch / condition / select
 * property sources in {@code assets/modernfoundry/items/*.json}). The registration entry points are kept as no-ops
 * so the call sites still compile; the visual state logic must live in the item model JSON.
 */
public class TinkerItemProperties {
  /** Registers the broken property for a tool (now data-driven via the item model) */
  public static void registerBrokenProperty(Item item) {}

  /** Registers tool properties including charge/block animations (now data-driven via the item model) */
  public static void registerToolProperties(ItemLike itemlike) {}

  /** Registers crossbow properties (now data-driven via the item model) */
  public static void registerCrossbowProperties(ItemLike item) {}
}
