package modernmods.modernfoundry.tools.modules;

import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.CraftCountModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module for changing how much ammo is crafted in an ammo modifier. Note this cannot increase the result above the tool stack size. */
public record CraftCountModule(LevelingValue multiplier, ModifierCondition<IToolStackView> condition) implements ModifierModule, CraftCountModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<CraftCountModule>defaultHooks(ModifierHooks.CRAFT_COUNT);
  public static final RecordLoadable<CraftCountModule> LOADER = RecordLoadable.create(LevelingValue.LOADABLE.requiredField("multiplier", CraftCountModule::multiplier), ModifierCondition.TOOL_FIELD, CraftCountModule::new);

  public CraftCountModule(LevelingValue multiplier) {
    this(multiplier, ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<CraftCountModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float modifyCraftCount(IToolStackView tool, ModifierEntry entry, float amount) {
    if (condition.matches(tool, entry)) {
      amount *= multiplier.compute(entry.getEffectiveLevel());
    }
    return amount;
  }
}
