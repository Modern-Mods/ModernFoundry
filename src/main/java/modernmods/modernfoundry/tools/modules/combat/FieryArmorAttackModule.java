package modernmods.modernfoundry.tools.modules.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.damage.DamageSourcePredicate;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.DamageDealtModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module for lighting the target on fire after an attack wearing this as armor */
public record FieryArmorAttackModule(LevelingInt time, IJsonPredicate<DamageSource> damageSource) implements ModifierModule, DamageDealtModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FieryArmorAttackModule>defaultHooks(ModifierHooks.DAMAGE_DEALT);
  public static final RecordLoadable<FieryArmorAttackModule> LOADER = RecordLoadable.create(
    LevelingInt.LOADABLE.requiredField("seconds", FieryArmorAttackModule::time),
    DamageSourcePredicate.LOADER.defaultField("damage_source", FieryArmorAttackModule::damageSource),
    FieryArmorAttackModule::new);

  @Override
  public RecordLoadable<FieryArmorAttackModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onDamageDealt(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, LivingEntity target, DamageSource source, float amount, boolean isDirectDamage) {
    if (this.damageSource.matches(source)) {
      target.igniteForSeconds(time.compute(modifier));
    }
  }
}
