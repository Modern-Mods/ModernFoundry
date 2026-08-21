package modernmods.modernfoundry.tools.yoyo;

import modernmods.modernfoundry.tools.yoyo.api.BlockInteraction;
import modernmods.modernfoundry.tools.yoyo.api.EntityInteraction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Tier;

/** Reference yoyo tier values, kept as native code instead of an external config file. */
public final class YoyoTier {
  private final String name;
  private final double weight;
  private final double length;
  private final int duration;
  private final double damage;
  private final Tier tier;
  private final List<BlockInteraction> blockInteractions = new ArrayList<>();
  private final List<EntityInteraction> entityInteractions = new ArrayList<>();

  public YoyoTier(String name, double weight, double length, int duration, double damage, Tier tier) {
    this.name = name;
    this.weight = weight;
    this.length = length;
    this.duration = duration;
    this.damage = damage;
    this.tier = tier;
  }

  public String getName() { return name; }
  public double getWeight() { return weight; }
  public double getLength() { return length; }
  public int getDuration() { return duration; }
  public double getDamage() { return damage; }
  public Tier getTier() { return tier; }
  public List<BlockInteraction> getBlockInteractions() { return blockInteractions; }
  public List<EntityInteraction> getEntityInteractions() { return entityInteractions; }

  public YoyoTier addBlockInteraction(BlockInteraction... interactions) {
    blockInteractions.addAll(List.of(interactions));
    return this;
  }

  public YoyoTier addEntityInteraction(EntityInteraction... interactions) {
    entityInteractions.addAll(List.of(interactions));
    return this;
  }
}
