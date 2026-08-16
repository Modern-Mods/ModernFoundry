package modernmods.modernfoundry.library.modifiers.modules.behavior;

import net.minecraft.world.entity.EquipmentSlot;
import modernmods.hilt.data.loadable.primitive.EnumLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.technical.ArmorLevelModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataKeys;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.function.Function;

/** Module to show the offhand for a tool that can interact using the offhand */
public enum ShowOffhandModule implements ModifierModule, EquipmentChangeModifierHook {
  /** Mode which will always show the offhand */
  ALLOW_BROKEN,
  /** Mode which will only show the offhand when the tool is not broken */
  DISALLOW_BROKEN;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ShowOffhandModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final RecordLoadable<ShowOffhandModule> LOADER = RecordLoadable.create(new EnumLoadable<>(ShowOffhandModule.class).requiredField("mode", Function.identity()), Function.identity());

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (context.getChangedSlot() == EquipmentSlot.CHEST && (!tool.isBroken() || this == ALLOW_BROKEN)) {
      ArmorLevelModule.addLevels(context, TinkerDataKeys.SHOW_EMPTY_OFFHAND, 1);
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (context.getChangedSlot() == EquipmentSlot.CHEST && (!tool.isBroken() || this == ALLOW_BROKEN)) {
      ArmorLevelModule.addLevels(context, TinkerDataKeys.SHOW_EMPTY_OFFHAND, -1);
    }
  }

  @Override
  public RecordLoadable<ShowOffhandModule> getLoader() {
    return LOADER;
  }
}
