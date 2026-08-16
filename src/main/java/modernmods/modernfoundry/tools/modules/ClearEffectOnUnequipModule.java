package modernmods.modernfoundry.tools.modules;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.record.RecordLoadable;
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

/** Module to reduce duration of effects on unequip. Used to prevent an exploit with {@link modernmods.modernfoundry.shared.TinkerAttributes#GOOD_EFFECT_DURATION} */
public record ClearEffectOnUnequipModule(MobEffect effect, ModifierCondition<IToolStackView> condition) implements ModifierModule, EquipmentChangeModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ClearEffectOnUnequipModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final RecordLoadable<ClearEffectOnUnequipModule> LOADER = RecordLoadable.create(
    Loadables.MOB_EFFECT.requiredField("effect", ClearEffectOnUnequipModule::effect),
    ModifierCondition.TOOL_FIELD,
    ClearEffectOnUnequipModule::new);

  @Override
  public RecordLoadable<ClearEffectOnUnequipModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    LivingEntity entity = context.getEntity();
    if (!entity.level().isClientSide && condition.matches(tool, modifier) && EquipmentChangeModifierHook.didUnequip(tool, context)) {
      entity.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
    }
  }
}
