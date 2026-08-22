package modernmods.modernfoundry.integrations.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Player-backed Botania set flags. */
public final class BotaniaSet {
  private final Player player;

  BotaniaSet(Player player) {
    this.player = player;
  }

  public void setTerrestrial(boolean value) { set("terrestrial", value); }
  public boolean hasTerrestrial() { return get("terrestrial"); }
  public void setGreatFairy(boolean value) { set("great_fairy", value); }
  public boolean hasGreatFairy() { return get("great_fairy"); }
  public void setAlfheim(boolean value) { set("alfheim", value); }
  public boolean hasAlfheim() { return get("alfheim"); }

  private boolean get(String key) {
    return CapabilityRegistry.getTag(player).getBoolean(key);
  }

  private void set(String key, boolean value) {
    CompoundTag tag = CapabilityRegistry.getTag(player);
    tag.putBoolean(key, value);
    CapabilityRegistry.saveTag(player, tag);
  }
}
