package modernmods.modernfoundry.integrations.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Player-backed Ars Elemental set flags. */
public final class ArsElementalSet {
  private final Player player;

  ArsElementalSet(Player player) {
    this.player = player;
  }

  public void setAir(boolean value) { set("air", value); }
  public boolean hasAir() { return get("air"); }
  public void setAqua(boolean value) { set("aqua", value); }
  public boolean hasAqua() { return get("aqua"); }
  public void setEarth(boolean value) { set("earth", value); }
  public boolean hasEarth() { return get("earth"); }
  public void setFire(boolean value) { set("fire", value); }
  public boolean hasFire() { return get("fire"); }

  private boolean get(String key) {
    return CapabilityRegistry.getTag(player).getBoolean(key);
  }

  private void set(String key, boolean value) {
    CompoundTag tag = CapabilityRegistry.getTag(player);
    tag.putBoolean(key, value);
    CapabilityRegistry.saveTag(player, tag);
  }
}
