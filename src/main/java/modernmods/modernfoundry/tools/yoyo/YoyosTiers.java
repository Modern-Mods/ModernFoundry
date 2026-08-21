package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.world.item.Tiers;

/** The official Yoyos 1.21.1 defaults. */
public final class YoyosTiers {
  private YoyosTiers() {}

  public static final YoyoTier WOODEN = tier("wooden", 2.2, 6.0, 100, 3.0, Tiers.WOOD);
  public static final YoyoTier STONE = tier("stone", 4.0, 7.0, 200, 4.0, Tiers.STONE);
  public static final YoyoTier COPPER = tier("copper", 4.5, 7.5, 250, 4.5, Tiers.GOLD);
  public static final YoyoTier IRON = tier("iron", 5.0, 8.0, 300, 5.0, Tiers.IRON);
  public static final YoyoTier GOLD = tier("gold", 6.5, 11.0, 450, 4.5, Tiers.GOLD);
  public static final YoyoTier DIAMOND = tier("diamond", 4.0, 9.0, 500, 6.0, Tiers.DIAMOND);
  public static final YoyoTier NETHERITE = tier("netherite", 6.2, 10.0, 600, 7.0, Tiers.NETHERITE);
  public static final YoyoTier CREATIVE = tier("creative", 0.9, 24.0, -1, 256.0, Tiers.NETHERITE);

  private static YoyoTier tier(String name, double weight, double length, int duration, double damage, net.minecraft.world.item.Tier tier) {
    return new YoyoTier(name, weight, length, duration, damage, tier)
      .addBlockInteraction(Interaction::breakBlocks, Interaction::craftWithBlock)
      .addEntityInteraction(Interaction::attackEntity, Interaction::collectItem);
  }
}
