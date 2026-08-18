package modernmods.modernfoundry.tools.stats;

import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import modernmods.mantle.util.RegistryHelper;
import modernmods.modernfoundry.common.TinkerTags;

import javax.annotation.Nullable;
import java.util.Locale;

/** Helper for registering the different effects for modifiers that change behavior based on the tool type */
public enum ToolType implements StringRepresentable {
  /** Held melee weapons such as swords, does not include unarmed. */
  MELEE(TinkerTags.Items.MELEE_WEAPON),
  /** Block breaking tools such as pickaxes or swords */
  HARVEST(TinkerTags.Items.HARVEST),
  /** Ranged tools that support velocity and drawspeed */
  RANGED(TinkerTags.Items.RANGED),
  /** Ranged tools that support velocity, drawspeed, and power */
  LAUNCHER(TinkerTags.Items.LAUNCHERS),
  /** Defensive items, including held and worn armor */
  ARMOR(TinkerTags.Items.ARMOR);

  public static final ToolType[] NO_MELEE = {HARVEST, RANGED, ARMOR};

  private final TagKey<Item> tag;
  private final String serializedName = name().toLowerCase(Locale.ROOT);

  ToolType(TagKey<Item> tag) {
    this.tag = tag;
  }

  public TagKey<Item> getTag() {
    return this.tag;
  }

  @Override
  public String getSerializedName() {
    return this.serializedName;
  }

  @Nullable
  public static ToolType from(Item item, ToolType... types) {
    for (ToolType type : types) {
      if (RegistryHelper.contains(type.tag, item)) {
        return type;
      }
    }
    return null;
  }
}
