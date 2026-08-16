package modernmods.modernfoundry.tools.modules.armor;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.modules.technical.SlotInChargeModule;
import modernmods.modernfoundry.library.modifiers.modules.technical.SlotInChargeModule.SlotInCharge;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import java.util.List;

/** Module implementing springy */
public record KnockbackCounterModule(TinkerDataKey<SlotInCharge> slotInCharge, LevelingValue chance, LevelingValue flat, LevelingValue random, int durabilityUsage, IJsonPredicate<LivingEntity> defender, IJsonPredicate<LivingEntity> attacker, ModifierCondition<IToolStackView> condition) implements CounterModule, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<KnockbackCounterModule>defaultHooks(ModifierHooks.ON_ATTACKED);
  public static final RecordLoadable<KnockbackCounterModule> LOADER = RecordLoadable.create(
    ContextKey.ID.mappedField((id, error) -> TinkerDataKey.of(id)),
    CounterModule.CHANCE_FIELD,
    LevelingValue.LOADABLE.defaultField("flat_knockback", LevelingValue.ZERO, KnockbackCounterModule::flat),
    LevelingValue.LOADABLE.defaultField("random_knockback", LevelingValue.ZERO, KnockbackCounterModule::random),
    CounterModule.DURABILITY_FIELD, CounterModule.DEFENDER_FIELD, CounterModule.ATTACKER_FIELD,
    ModifierCondition.TOOL_FIELD,
    KnockbackCounterModule::new);

  /** @apiNote use {@link #builder()} */
  @Internal
  public KnockbackCounterModule {}

  /** Creates a new builder instance */
  public static CounterModule.Builder<KnockbackCounterModule> builder() {
    return new CounterModule.Builder<>((chance, flat, random, durabilityUsage, defender, attacker,condition)
      -> new KnockbackCounterModule(TConstruct.createKey("dummy"), chance, flat, random, durabilityUsage, defender, attacker, condition));
  }

  // TODO 1.21: remove flat in favor of this
  @Override
  public LevelingValue constant() {
    return flat;
  }

  @Override
  public RecordLoadable<KnockbackCounterModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addModules(ModuleHookMap.Builder builder) {
    builder.addModule(new SlotInChargeModule(slotInCharge));
  }

  @Deprecated
  @Override
  public void applyEffect(IToolStackView tool, ModifierEntry modifier, float value, EquipmentContext context, Entity attacker, DamageSource source, float damageDealt) {}

  @SuppressWarnings("PatternVariableHidesField")
  @Override
  public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float damageDealt, boolean isDirectDamage) {
    // direct damage on the server against a living entity
    // only run once across all pieces, as we want a max effect and knockback doesn't naturally max
    LivingEntity defender = context.getEntity();
    if (isDirectDamage && !defender.level().isClientSide && condition.matches(tool, modifier) && this.defender.matches(defender) && source.getEntity() instanceof LivingEntity attacker && this.attacker.matches(attacker) && SlotInChargeModule.isInCharge(context.getTinkerData(), slotInCharge, slotType)) {
      // figure out which slot is blocking, it gets its effect doubled
      EquipmentSlot blockingSlot = null;
      if (defender.isUsingItem()) {
        EquipmentSlot checkSlot = Util.getSlotType(defender.getUsedItemHand());
        IToolStackView blockingTool = context.getValidTool(checkSlot);
        // TODO: CounterModule.isBlocking?
        if (blockingTool != null && ModifierUtil.canPerformAction(blockingTool, ItemAbilities.SHIELD_BLOCK) && defender.getItemBySlot(checkSlot).getUseDuration(defender) - defender.getUseItemRemainingTicks() >= 5) {
          blockingSlot = checkSlot;
        }
      }

      // each slot attempts to apply, we keep the largest one, consistent with other counterattack modifiers
      float bestBonus = 0;
      for (EquipmentSlot bouncingSlot : EquipmentSlot.values()) {
        IToolStackView bouncingTool = context.getToolInSlot(bouncingSlot);
        if (bouncingTool != null && !bouncingTool.isBroken() && bouncingTool.hasTag(TinkerTags.Items.ARMOR)) {
          float level = modifier.getEffectiveLevel();
          if (bouncingSlot == blockingSlot) {
            level *= 2;
          }
          if (TConstruct.RANDOM.nextFloat() < chance.compute(level)) {
            float newBonus = LevelingValue.applyRandom(level, flat, random);
            if (newBonus > bestBonus) {
              bestBonus = newBonus;
            }
            // all tools that contributed get damaged, consistency with other counter modules
            if (durabilityUsage > 0 && newBonus > 0) {
              ToolDamageUtil.damageAnimated(bouncingTool, durabilityUsage, defender, bouncingSlot, modifier.getId());
            }
          }
        }
      }
      // did we end up with any bonus?
      if (bestBonus > 0) {
        float angle = attacker.getYRot() * (float)Math.PI / 180F;
        attacker.knockback(bestBonus, -Mth.sin(angle), Mth.cos(angle));
      }
    }
  }
}
