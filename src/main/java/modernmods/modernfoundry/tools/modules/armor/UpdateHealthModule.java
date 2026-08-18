package modernmods.modernfoundry.tools.modules.armor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.util.CombatHelper;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.DamageOnUnequipModule;

import java.util.List;
import java.util.Set;

/**
 * Module to refresh help when changing equipment
 * @param bonus      Amount of extra health to add when equipping this. Removed when unequipping.
 * @param slots      Slots that apply this module
 * @param condition  Common modifier conditions
 */
public record UpdateHealthModule(LevelingValue bonus, Set<EquipmentSlot> slots, ModifierCondition<IToolStackView> condition) implements ModifierModule, EquipmentChangeModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<DamageOnUnequipModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final RecordLoadable<UpdateHealthModule> LOADER = RecordLoadable.create(
    LevelingValue.LOADABLE.defaultField("bonus", LevelingValue.ZERO, false, UpdateHealthModule::bonus),
    TinkerLoadables.EQUIPMENT_SLOT_SET.requiredField("slots", UpdateHealthModule::slots),
    ModifierCondition.TOOL_FIELD,
    UpdateHealthModule::new);

  public UpdateHealthModule(LevelingValue bonus, EquipmentSlot... slots) {
    this(bonus, ImmutableSet.copyOf(slots), ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<UpdateHealthModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /** Updates health on the target on change */
  private void updateHealth(LivingEntity entity, float bonus) {
    float maxHealth = entity.getMaxHealth();
    float newHealth = entity.getHealth() + bonus;
    // ensure new health is not greater than max
    if (newHealth > maxHealth) {
      entity.setHealth(maxHealth);
    } else if (bonus > 0) {
      // if healing, grant immediately
      entity.setHealth(newHealth);
    } else if (bonus < 0) {
      // if harming, apply using a hurt to trigger a death message if it kills you
      entity.hurt(CombatHelper.damageSource(entity.level(), TinkerDamageTypes.UPDATE_HEALTH), -bonus);
    }
  }

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (condition.matches(tool, modifier) && slots.contains(context.getChangedSlot())) {
      Level level = context.getLevel();
      if (!level.isClientSide() && EquipmentChangeModifierHook.didEquip(tool, context)) {
        updateHealth(context.getEntity(), bonus.compute(modifier));
      }
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (condition.matches(tool, modifier) && slots.contains(context.getChangedSlot())) {
      Level level = context.getLevel();
      if (!level.isClientSide() && EquipmentChangeModifierHook.didUnequip(tool, context)) {
        updateHealth(context.getEntity(), -bonus.compute(modifier));
      }
    }
  }
}
