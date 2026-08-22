package modernmods.modernfoundry.integrations.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/** Native persistent replacement for the old Forge capability attachment. */
public final class CapabilityRegistry {
  private static final String ROOT = "modernfoundry:integration_sets";

  private CapabilityRegistry() {}

  public static BotaniaSet botania(Player player) {
    return new BotaniaSet(player);
  }

  public static Optional<BotaniaSet> getBotania(Player player) {
    return Optional.of(botania(player));
  }

  public static ArsElementalSet arsElemental(Player player) {
    return new ArsElementalSet(player);
  }

  public static Optional<ArsElementalSet> getArsElemental(Player player) {
    return Optional.of(arsElemental(player));
  }

  static CompoundTag getTag(Player player) {
    CompoundTag root = player.getPersistentData().getCompound(ROOT);
    player.getPersistentData().put(ROOT, root);
    return root;
  }

  static void saveTag(Player player, CompoundTag tag) {
    player.getPersistentData().put(ROOT, tag);
  }
}
