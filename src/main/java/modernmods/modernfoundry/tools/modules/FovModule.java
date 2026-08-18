package modernmods.modernfoundry.tools.modules;

import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.primitive.EnumLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.util.LogicHelper;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataKeys;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module implementing {@link modernmods.modernfoundry.tools.data.ModifierIds#nearsighted} and {@link modernmods.modernfoundry.tools.data.ModifierIds#farsighted} */
public record FovModule(LevelingValue value, FovAction action) implements ModifierModule, EquipmentChangeModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FovModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final RecordLoadable<FovModule> LOADER = RecordLoadable.create(
    LevelingValue.LOADABLE.directField(FovModule::value),
    new EnumLoadable<>(FovAction.class).requiredField("action", FovModule::action),
    FovModule::new);

  @Override
  public RecordLoadable<FovModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /** Gets the key for the given context */
  private static Identifier getKey(ModifierEntry modifier, EquipmentChangeContext context) {
    return modifier.getId().withSuffix('_' + context.getChangedSlot().getName());
  }

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!tool.isBroken()) {
      TinkerDataCapability.Holder data = LogicHelper.orElseNull(context.getTinkerData().resolve());
      if (data != null) {
        data.computeIfAbsent(TinkerDataKeys.FOV_MODIFIER).set(getKey(modifier, context), action.apply(value.compute(modifier.getEffectiveLevel())));
      }
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!tool.isBroken()) {
      TinkerDataCapability.Holder data = LogicHelper.orElseNull(context.getTinkerData().resolve());
      if (data != null) {
        data.computeIfAbsent(TinkerDataKeys.FOV_MODIFIER).remove(getKey(modifier, context));
      }
    }
  }

  /** Represents whether we decrease or increase FOV */
  public enum FovAction {
    INCREASE {
      @Override
      public float apply(float amount) {
        return 1 + amount;
      }
    },
    DECREASE {
      @Override
      public float apply(float amount) {
        return 1 / (1 + amount);
      }
    };

    public abstract float apply(float amount);
  }
}
