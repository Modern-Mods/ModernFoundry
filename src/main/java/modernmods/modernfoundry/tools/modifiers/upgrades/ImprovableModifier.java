package modernmods.modernfoundry.tools.modifiers.upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ArmorWalkModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.ElytraFlightModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ToolStatsModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.VolatileDataModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockBreakModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.BlockTransformModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.PlantHarvestModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.ShearsModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.data.ModifierIds;
import modernmods.modernfoundry.tools.logic.ToolLevellingUtil;

import javax.annotation.Nullable;
import java.util.List;

/** XP-driven modifier that adds persistent tool levels, slots, and optional stat rewards. */
public class ImprovableModifier extends NoLevelsModifier implements PlantHarvestModifierHook, ShearsModifierHook,
    BlockBreakModifierHook, BlockTransformModifierHook, ProjectileLaunchModifierHook, OnAttackedModifierHook,
    MeleeHitModifierHook, ElytraFlightModifierHook, ArmorWalkModifierHook, ModifierRemovalHook,
    VolatileDataModifierHook, ToolStatsModifierHook {
  public static final ResourceLocation EXPERIENCE_KEY = ToolLevellingUtil.EXPERIENCE_KEY;
  public static final ResourceLocation LEVEL_KEY = ToolLevellingUtil.LEVEL_KEY;
  public static final ResourceLocation SLOT_HISTORY_KEY = ToolLevellingUtil.SLOT_HISTORY_KEY;
  public static final ResourceLocation STAT_HISTORY_KEY = ToolLevellingUtil.STAT_HISTORY_KEY;

  @Override
  protected void registerHooks(Builder hooks) {
    super.registerHooks(hooks);
    hooks.addHook(this, ModifierHooks.PLANT_HARVEST, ModifierHooks.SHEAR_ENTITY, ModifierHooks.BLOCK_TRANSFORM,
      ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.BLOCK_BREAK, ModifierHooks.ON_ATTACKED,
      ModifierHooks.MELEE_HIT, ModifierHooks.ELYTRA_FLIGHT, ModifierHooks.BOOT_WALK,
      ModifierHooks.VOLATILE_DATA, ModifierHooks.TOOL_STATS, ModifierHooks.REMOVE);
  }

  @Override
  public int getPriority() {
    return 300;
  }

  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    tool.getPersistentData().remove(EXPERIENCE_KEY);
    tool.getPersistentData().remove(LEVEL_KEY);
    tool.getPersistentData().remove(SLOT_HISTORY_KEY);
    tool.getPersistentData().remove(STAT_HISTORY_KEY);
    return null;
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    if (!ToolLevellingUtil.isSlotsLevellingEnabled(context)) {
      return;
    }
    for (String slot : ToolLevellingUtil.parseHistory(context.getPersistentData().getString(SLOT_HISTORY_KEY))) {
      volatileData.addSlots(ToolLevellingUtil.slotType(slot), 1);
    }
  }

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    if (!ToolLevellingUtil.isStatsLevellingEnabled(context)) {
      return;
    }
    for (String name : ToolLevellingUtil.parseHistory(context.getPersistentData().getString(STAT_HISTORY_KEY))) {
      FloatToolStat stat = ToolLevellingUtil.statType(name);
      if (stat != null) {
        stat.add(builder, ToolLevellingUtil.getStatValue(context, name));
      }
    }
  }

  @Override
  public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
    if (Config.COMMON.improvableMiningXp.get() && context.canHarvest() && context.isEffective()) {
      ToolLevellingUtil.addExperience(tool instanceof ToolStack stack ? stack : null,
        1 + Config.COMMON.improvableBonusMiningXp.get(), context.getPlayer());
    }
  }

  @Override
  public void afterHarvest(IToolStackView tool, ModifierEntry modifier, UseOnContext context,
                           net.minecraft.server.level.ServerLevel world, BlockState state, BlockPos pos) {
    if (Config.COMMON.improvableHarvestingXp.get() && context.getPlayer() instanceof ServerPlayer player) {
      ToolLevellingUtil.addExperience(ToolLevellingUtil.findHeldTool(player, tool, toolSlot(context)),
        1 + Config.COMMON.improvableBonusHarvestingXp.get(), player);
    }
  }

  @Override
  public void afterShearEntity(IToolStackView tool, ModifierEntry modifier, Player player, net.minecraft.world.entity.Entity entity, boolean isTarget) {
    if (Config.COMMON.improvableShearingXp.get() && player instanceof ServerPlayer serverPlayer) {
      ToolLevellingUtil.addExperience(ToolLevellingUtil.findHeldTool(serverPlayer, tool, EquipmentSlot.MAINHAND),
        1 + Config.COMMON.improvableBonusShearingXp.get(), serverPlayer);
    }
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    if (!Config.COMMON.improvableAttackingXp.get() || !(context.getPlayerAttacker() instanceof ServerPlayer player)
      || context.getLivingTarget() == null || context.getTarget() instanceof ArmorStand
      || (!Config.COMMON.improvableEnablePvp.get() && context.getLivingTarget() instanceof Player)) {
      return;
    }
    int xp = Config.COMMON.improvableDamageDealt.get() ? Math.max(1, Math.round(damageDealt)) : 1;
    ToolLevellingUtil.addExperience(ToolLevellingUtil.findHeldTool(player, tool, context.getSlotType()),
      xp + Config.COMMON.improvableBonusAttackingXp.get(), player);
  }

  @Override
  public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slot,
                         DamageSource source, float amount, boolean isDirectDamage) {
    if (!Config.COMMON.improvableTakingDamageXp.get() || !slot.isArmor()
      || !isDirectDamage || !(context.getEntity() instanceof ServerPlayer player)
      || source.is(DamageTypeTags.BYPASSES_ARMOR) || !(source.getEntity() instanceof LivingEntity attacker)
      || attacker == player || (!Config.COMMON.improvableEnablePvp.get() && attacker instanceof Player)) {
      return;
    }
    int xp = Config.COMMON.improvableDamageTaken.get() ? Math.max(1, Math.round(amount)) : 1;
    ToolLevellingUtil.addExperience(ToolLevellingUtil.getHeldTool(player, slot),
      xp + Config.COMMON.improvableBonusTakingDamageXp.get() + thornsBonus(tool), player);
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile,
                                 @Nullable AbstractArrow arrow, modernmods.modernfoundry.library.tools.nbt.ModDataNBT data,
                                 boolean primary) {
    if (Config.COMMON.improvableShootingXp.get() && shooter instanceof ServerPlayer player) {
      ToolLevellingUtil.addExperience(ToolLevellingUtil.findHeldTool(player, tool, EquipmentSlot.MAINHAND),
        1 + Config.COMMON.improvableBonusShootingXp.get(), player);
    }
  }

  @Override
  public void afterTransformBlock(IToolStackView tool, ModifierEntry modifier, UseOnContext context,
                                  BlockState state, BlockPos pos, ItemAbility action) {
    if (!(context.getPlayer() instanceof ServerPlayer player)) {
      return;
    }
    int bonus = action.equals(ItemAbilities.AXE_STRIP) && Config.COMMON.improvableStrippingXp.get() ? Config.COMMON.improvableBonusStrippingXp.get() : -1;
    if (action.equals(ItemAbilities.AXE_SCRAPE) && Config.COMMON.improvableScrappingXp.get()) bonus = Config.COMMON.improvableBonusScrappingXp.get();
    if (action.equals(ItemAbilities.AXE_WAX_OFF) && Config.COMMON.improvableWaxingOffXp.get()) bonus = Config.COMMON.improvableBonusWaxingOffXp.get();
    if (action.equals(ItemAbilities.HOE_TILL) && Config.COMMON.improvableTillingXp.get()) bonus = Config.COMMON.improvableBonusTillingXp.get();
    if (action.equals(ItemAbilities.SHOVEL_FLATTEN) && Config.COMMON.improvablePathMakingXp.get()) bonus = Config.COMMON.improvableBonusPathMakingXp.get();
    if (bonus >= 0) {
      ToolLevellingUtil.addExperience(ToolLevellingUtil.findHeldTool(player, tool, toolSlot(context)), 1 + bonus, player);
    }
  }

  @Override
  public boolean elytraFlightTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int flightTicks) {
    if (Config.COMMON.improvableFlyingXp.get() && entity instanceof ServerPlayer player
      && flightTicks > 0 && flightTicks % Config.COMMON.improvableFlyingTime.get() == 0) {
      ToolLevellingUtil.addExperience(ToolLevellingUtil.getHeldTool(player, EquipmentSlot.CHEST),
        1 + Config.COMMON.improvableBonusFlyingXp.get(), player);
    }
    return false;
  }

  @Override
  public void onWalk(IToolStackView tool, ModifierEntry modifier, LivingEntity living, BlockPos previous, BlockPos current) {
    if (!(living instanceof ServerPlayer player) || !player.onGround() || previous.equals(current) || tool.isBroken()) {
      return;
    }
    int bonus = 0;
    if (Config.COMMON.improvablePlowingXp.get() && tool.getModifierLevel(ModifierIds.tilling) > 0) {
      bonus = Config.COMMON.improvableBonusPlowingXp.get();
    } else if (Config.COMMON.improvablePathMakerXp.get() && tool.getModifierLevel(ModifierIds.pathing) > 0) {
      bonus = Config.COMMON.improvableBonusPathMakerXp.get();
    } else if (Config.COMMON.improvableSnowdriftXp.get() && tool.getModifierLevel(ModifierIds.snowdrift) > 0) {
      bonus = Config.COMMON.improvableBonusSnowdriftXp.get();
    } else if (Config.COMMON.improvableFlamewakeXp.get() && tool.getModifierLevel(ModifierIds.flamewake) > 0) {
      bonus = Config.COMMON.improvableBonusFlamewakeXp.get();
    } else if (Config.COMMON.improvableFrostWalkerXp.get() && tool.getModifierLevel(ModifierIds.frostWalker) > 0) {
      bonus = Config.COMMON.improvableBonusFrostWalkerXp.get();
    } else if (Config.COMMON.improvableGlowingXp.get() && tool.getModifierLevel(ModifierIds.glowing) > 0) {
      bonus = Config.COMMON.improvableBonusGlowingXp.get();
    }
    if (bonus >= 0) {
      ToolLevellingUtil.addExperience((ToolStack) tool, 1 + bonus, player);
    }
  }

  private static EquipmentSlot toolSlot(UseOnContext context) {
    return context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
  }

  private static int thornsBonus(IToolStackView tool) {
    int thorns = tool.getModifierLevel(ModifierIds.thorns);
    if (!Config.COMMON.improvableThornsXp.get() || thorns <= 0 || Modifier.RANDOM.nextFloat() >= thorns * 0.15f) {
      return 0;
    }
    return 1 + Modifier.RANDOM.nextInt(Config.COMMON.improvableBonusThornsXp.get() + 1);
  }
}
