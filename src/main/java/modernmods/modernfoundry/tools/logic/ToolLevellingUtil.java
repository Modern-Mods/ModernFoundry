package modernmods.modernfoundry.tools.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.SlotType;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.data.ModifierIds;
import modernmods.modernfoundry.tools.network.LevelUpPacket;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/** Persistent progression and reward calculations for the improvable modifier. */
public final class ToolLevellingUtil {
  public static final ResourceLocation EXPERIENCE_KEY = modernmods.modernfoundry.TConstruct.getResource("experience");
  public static final ResourceLocation LEVEL_KEY = modernmods.modernfoundry.TConstruct.getResource("level");
  public static final ResourceLocation SLOT_HISTORY_KEY = modernmods.modernfoundry.TConstruct.getResource("slot_history");
  public static final ResourceLocation STAT_HISTORY_KEY = modernmods.modernfoundry.TConstruct.getResource("stat_history");

  public static final String UPGRADE = "upgrade";
  public static final String ABILITY = "ability";
  public static final String DEFENSE = "defense";
  public static final String SOUL = "soul";
  public static final String DURABILITY = "durability";
  public static final String ATTACK_DAMAGE = "attackDamage";
  public static final String ATTACK_SPEED = "attackSpeed";
  public static final String MINING_SPEED = "miningSpeed";
  public static final String ARMOR = "armor";
  public static final String ARMOR_TOUGHNESS = "armorToughness";
  public static final String KNOCKBACK_RESISTANCE = "knockbackResistance";
  public static final String DRAW_SPEED = "drawSpeed";
  public static final String VELOCITY = "velocity";
  public static final String ACCURACY = "accuracy";
  public static final String PROJECTILE_DAMAGE = "projectileDamage";

  private static final Random RANDOM = new Random();

  private ToolLevellingUtil() {}

  public static boolean hasImprovable(IToolContext tool) {
    return tool.getModifierLevel(TinkerModifiers.improvable.getId()) > 0;
  }

  public static boolean isStaff(IToolContext tool) {
    return tool.hasTag(TinkerTags.Items.STAFFS);
  }

  public static boolean isArmor(IToolContext tool) {
    return tool.hasTag(TinkerTags.Items.ARMOR);
  }

  public static boolean isRanged(IToolContext tool) {
    return tool.hasTag(TinkerTags.Items.RANGED);
  }

  public static boolean isBroadTool(IToolContext tool) {
    return tool.getMaterials().size() > 3 || isStaff(tool);
  }

  public static boolean canLevelUp(int level) {
    return Config.COMMON.improvableMaxLevel.get() == 0 || level < Config.COMMON.improvableMaxLevel.get();
  }

  public static int getXpNeededForLevel(int level, boolean broad) {
    long result = Config.COMMON.improvableBaseExperience.get();
    for (int i = 1; i < level; i++) {
      result = Math.min(Integer.MAX_VALUE, Math.round(result * Config.COMMON.improvableRequiredXpMultiplier.get()));
    }
    if (broad) {
      result = Math.min(Integer.MAX_VALUE, Math.round(result * Config.COMMON.improvableBroadToolRequiredXpMultiplier.get()));
    }
    return (int) result;
  }

  /** Adds server-authoritative XP and rebuilds stats once per level gained. */
  public static void addExperience(@Nullable ToolStack tool, int amount, @Nullable ServerPlayer player) {
    if (tool == null || amount <= 0 || !hasImprovable(tool) || !canLevelUp(level(tool))) {
      return;
    }
    ModDataNBT data = tool.getPersistentData();
    int currentLevel = Math.max(0, data.getInt(LEVEL_KEY));
    long experience = Math.max(0, (long) data.getInt(EXPERIENCE_KEY)) + amount;
    boolean broad = isBroadTool(tool);
    while (canLevelUp(currentLevel) && experience >= getXpNeededForLevel(currentLevel + 1, broad)) {
      experience -= getXpNeededForLevel(currentLevel + 1, broad);
      currentLevel++;
      String slot = getSlot(tool, currentLevel);
      if (slot != null) appendHistory(SLOT_HISTORY_KEY, slot, data);
      String stat = getStat(tool, currentLevel);
      if (stat != null) appendHistory(STAT_HISTORY_KEY, stat, data);
      data.putInt(LEVEL_KEY, currentLevel);
      data.putInt(EXPERIENCE_KEY, 0);
      tool.rebuildStats();
      if (player != null) {
        modernmods.modernfoundry.common.network.TinkerNetwork.getInstance().sendTo(
          new LevelUpPacket(currentLevel, tool.createStack().getDisplayName()), player);
      }
    }
    data.putInt(EXPERIENCE_KEY, (int) Math.min(Integer.MAX_VALUE, experience));
  }

  public static int level(IToolStackView tool) {
    return Math.max(0, tool.getPersistentData().getInt(LEVEL_KEY));
  }

  @Nullable
  public static ToolStack getHeldTool(net.minecraft.world.entity.LivingEntity living, EquipmentSlot slot) {
    if (living == null) return null;
    ItemStack stack = living.getItemBySlot(slot);
    if (stack.isEmpty() || !stack.is(TinkerTags.Items.MODIFIABLE)) return null;
    ToolStack tool = ToolStack.from(stack);
    return tool.isBroken() || !hasImprovable(tool) ? null : tool;
  }

  @Nullable
  public static ToolStack getHeldTool(net.minecraft.world.entity.LivingEntity living, InteractionHand hand) {
    return getHeldTool(living, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
  }

  @Nullable
  public static ToolStack findHeldTool(ServerPlayer player, IToolStackView target, EquipmentSlot preferred) {
    ToolStack preferredTool = getHeldTool(player, preferred);
    if (preferredTool != null && target.isSameStack(player.getItemBySlot(preferred))) return preferredTool;
    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
      ToolStack candidate = getHeldTool(player, slot);
      if (candidate != null && target.isSameStack(player.getItemBySlot(slot))) return candidate;
    }
    return null;
  }

  @Nullable
  public static String getSlot(IToolContext tool, int level) {
    Config.GainingMethod method = slotMethod(tool);
    if (method == Config.GainingMethod.NONE) return null;
    if (method == Config.GainingMethod.RANDOM) {
      List<String> pool = slotPool(tool);
      return pool.isEmpty() ? null : pool.get(RANDOM.nextInt(pool.size()));
    }
    List<String> order = slotOrder(tool);
    return order.isEmpty() ? null : order.get((level - 1) % order.size());
  }

  @Nullable
  public static String getStat(IToolContext tool, int level) {
    Config.GainingMethod method = statMethod(tool);
    if (method == Config.GainingMethod.NONE) return null;
    if (method == Config.GainingMethod.RANDOM) {
      List<String> pool = statOrder(tool);
      return pool.isEmpty() ? null : pool.get(RANDOM.nextInt(pool.size()));
    }
    List<String> order = statOrder(tool);
    return order.isEmpty() ? null : order.get((level - 1) % order.size());
  }

  public static boolean canPredictNextSlot(IToolContext tool) {
    return slotMethod(tool) == Config.GainingMethod.PREDEFINED_ORDER;
  }

  public static boolean canPredictNextStat(IToolContext tool) {
    return statMethod(tool) == Config.GainingMethod.PREDEFINED_ORDER;
  }

  public static SlotType slotType(String name) {
    return switch (name) {
      case UPGRADE -> SlotType.UPGRADE;
      case ABILITY -> SlotType.ABILITY;
      case DEFENSE -> SlotType.DEFENSE;
      case SOUL -> SlotType.SOUL;
      default -> throw new IllegalArgumentException("Unsupported improvable slot " + name);
    };
  }

  @Nullable
  public static FloatToolStat statType(String name) {
    return switch (name) {
      case DURABILITY -> ToolStats.DURABILITY;
      case ATTACK_DAMAGE -> ToolStats.ATTACK_DAMAGE;
      case ATTACK_SPEED -> ToolStats.ATTACK_SPEED;
      case MINING_SPEED -> ToolStats.MINING_SPEED;
      case ARMOR -> ToolStats.ARMOR;
      case ARMOR_TOUGHNESS -> ToolStats.ARMOR_TOUGHNESS;
      case KNOCKBACK_RESISTANCE -> ToolStats.KNOCKBACK_RESISTANCE;
      case DRAW_SPEED -> ToolStats.DRAW_SPEED;
      case VELOCITY -> ToolStats.VELOCITY;
      case ACCURACY -> ToolStats.ACCURACY;
      case PROJECTILE_DAMAGE -> ToolStats.PROJECTILE_DAMAGE;
      default -> null;
    };
  }

  public static double getStatValue(IToolContext tool, String name) {
    FloatToolStat stat = statType(name);
    if (stat == null) return 0;
    if (isStaff(tool)) return staffValue(stat);
    if (isArmor(tool)) return armorValue(stat);
    if (isRanged(tool)) return rangedValue(stat);
    return toolValue(stat);
  }

  public static double getStatValue(IToolContext tool, FloatToolStat stat) {
    return getStatValue(tool, stat.getName().getPath());
  }

  public static List<String> parseHistory(String value) {
    return value.isBlank() ? List.of() : List.of(value.split(";"));
  }

  public static boolean isSlotsLevellingEnabled(IToolContext tool) {
    return slotMethod(tool) != Config.GainingMethod.NONE;
  }

  public static boolean isStatsLevellingEnabled(IToolContext tool) {
    return statMethod(tool) != Config.GainingMethod.NONE;
  }

  private static void appendHistory(ResourceLocation key, String value, ModDataNBT data) {
    data.putString(key, data.getString(key) + value + ";");
  }

  private static Config.GainingMethod slotMethod(IToolContext tool) {
    if (isStaff(tool)) return Config.COMMON.improvableStaffSlotGainingMethod.get();
    if (isArmor(tool)) return Config.COMMON.improvableArmorSlotGainingMethod.get();
    if (isRanged(tool)) return Config.COMMON.improvableRangedSlotGainingMethod.get();
    return Config.COMMON.improvableToolsSlotGainingMethod.get();
  }

  private static Config.GainingMethod statMethod(IToolContext tool) {
    if (isStaff(tool)) return Config.COMMON.improvableStaffStatGainingMethod.get();
    if (isArmor(tool)) return Config.COMMON.improvableArmorStatGainingMethod.get();
    if (isRanged(tool)) return Config.COMMON.improvableRangedStatGainingMethod.get();
    return Config.COMMON.improvableToolsStatGainingMethod.get();
  }

  private static List<String> slotOrder(IToolContext tool) {
    return strings(isStaff(tool) ? Config.COMMON.improvableStaffSlotOrder.get() : isArmor(tool) ? Config.COMMON.improvableArmorSlotOrder.get() : isRanged(tool) ? Config.COMMON.improvableRangedSlotOrder.get() : Config.COMMON.improvableToolsSlotOrder.get());
  }

  private static List<String> slotPool(IToolContext tool) {
    return slotOrder(tool);
  }

  private static List<String> statOrder(IToolContext tool) {
    return strings(isStaff(tool) ? Config.COMMON.improvableStaffStatOrder.get() : isArmor(tool) ? Config.COMMON.improvableArmorStatOrder.get() : isRanged(tool) ? Config.COMMON.improvableRangedStatOrder.get() : Config.COMMON.improvableToolsStatOrder.get());
  }

  private static List<String> strings(List<? extends String> values) {
    return values.stream().map(String::valueOf).toList();
  }

  private static double toolValue(FloatToolStat stat) {
    if (stat == ToolStats.DURABILITY) return Config.COMMON.improvableToolDurabilityValue.get();
    if (stat == ToolStats.ATTACK_DAMAGE) return Config.COMMON.improvableToolAttackDamageValue.get();
    if (stat == ToolStats.ATTACK_SPEED) return Config.COMMON.improvableToolAttackSpeedValue.get();
    if (stat == ToolStats.MINING_SPEED) return Config.COMMON.improvableToolMiningSpeedValue.get();
    return 0;
  }

  private static double rangedValue(FloatToolStat stat) {
    if (stat == ToolStats.DURABILITY) return Config.COMMON.improvableRangedDurabilityValue.get();
    if (stat == ToolStats.DRAW_SPEED) return Config.COMMON.improvableRangedDrawSpeedValue.get();
    if (stat == ToolStats.VELOCITY) return Config.COMMON.improvableRangedVelocityValue.get();
    if (stat == ToolStats.ACCURACY) return Config.COMMON.improvableRangedAccuracyValue.get();
    if (stat == ToolStats.PROJECTILE_DAMAGE) return Config.COMMON.improvableRangedProjectileDamageValue.get();
    if (stat == ToolStats.ATTACK_DAMAGE) return Config.COMMON.improvableRangedAttackDamageValue.get();
    if (stat == ToolStats.ATTACK_SPEED) return Config.COMMON.improvableRangedAttackSpeedValue.get();
    return 0;
  }

  private static double armorValue(FloatToolStat stat) {
    if (stat == ToolStats.DURABILITY) return Config.COMMON.improvableArmorDurabilityValue.get();
    if (stat == ToolStats.ARMOR) return Config.COMMON.improvableArmorValue.get();
    if (stat == ToolStats.ARMOR_TOUGHNESS) return Config.COMMON.improvableArmorToughnessValue.get();
    if (stat == ToolStats.KNOCKBACK_RESISTANCE) return Config.COMMON.improvableArmorKnockbackResistanceValue.get();
    return 0;
  }

  private static double staffValue(FloatToolStat stat) {
    if (stat == ToolStats.DURABILITY) return Config.COMMON.improvableStaffDurabilityValue.get();
    if (stat == ToolStats.DRAW_SPEED) return Config.COMMON.improvableStaffDrawSpeedValue.get();
    if (stat == ToolStats.VELOCITY) return Config.COMMON.improvableStaffVelocityValue.get();
    if (stat == ToolStats.ACCURACY) return Config.COMMON.improvableStaffAccuracyValue.get();
    if (stat == ToolStats.PROJECTILE_DAMAGE) return Config.COMMON.improvableStaffProjectileDamageValue.get();
    if (stat == ToolStats.ARMOR) return Config.COMMON.improvableStaffArmorValue.get();
    return 0;
  }
}
