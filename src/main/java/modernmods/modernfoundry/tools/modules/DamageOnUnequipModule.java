package modernmods.modernfoundry.tools.modules;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.primitive.FloatLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.common.TinkerDamageTypes;
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

import java.util.List;

/** Module to damage the player when taking off the equipment */
public record DamageOnUnequipModule(float damage, ResourceKey<DamageType> damageType, ModifierCondition<IToolStackView> condition) implements ModifierModule, EquipmentChangeModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<DamageOnUnequipModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final RecordLoadable<DamageOnUnequipModule> LOADER = RecordLoadable.create(
    FloatLoadable.FROM_ZERO.requiredField("damage", DamageOnUnequipModule::damage),
    Loadables.DAMAGE_TYPE_KEY.defaultField("damage_type", TinkerDamageTypes.ENTANGLED, true, DamageOnUnequipModule::damageType),
    ModifierCondition.TOOL_FIELD,
    DamageOnUnequipModule::new);

  public DamageOnUnequipModule(float damage, ModifierCondition<IToolStackView> condition) {
    this(damage, TinkerDamageTypes.ENTANGLED, condition);
  }

  @Override
  public RecordLoadable<DamageOnUnequipModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (condition.matches(tool, modifier)) {
      Level level = context.getLevel();
      if (!level.isClientSide() && EquipmentChangeModifierHook.didUnequip(tool, context)) {
        context.getEntity().hurt(TinkerDamageTypes.source(level.registryAccess(), damageType), damage * modifier.getEffectiveLevel());
      }
    }
  }
}
