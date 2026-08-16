package modernmods.modernfoundry.tools.modules.durability;

import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.special.CapacityBarHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import java.util.List;

/**
 * Module connecting normal tool durability to {@link CapacityBarHook}. Meant to be used on the specific modifier rather than an internal modifier.
 * TODO 1.21: Move to {@link modernmods.modernfoundry.library.modifiers.modules.capacity}.
 */
public enum DurabilityAsCapacityModule implements ModifierModule, CapacityBarHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<DurabilityAsCapacityModule>defaultHooks(ModifierHooks.CAPACITY_BAR);
  public static final RecordLoadable<DurabilityAsCapacityModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<DurabilityAsCapacityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public int getAmount(IToolStackView tool) {
    return tool.getCurrentDurability();
  }

  @Override
  public int getCapacity(IToolStackView tool, ModifierEntry entry) {
    return tool.getStats().getInt(ToolStats.DURABILITY);
  }

  @Override
  public void setAmount(IToolStackView tool, ModifierEntry entry, int amount) {
    tool.setDamage(tool.getStats().getInt(ToolStats.DURABILITY) - amount);
  }

  @Override
  public void addAmount(IToolStackView tool, ModifierEntry modifier, int amount) {
    tool.setDamage(tool.getDamage() - amount);
  }

  @Override
  public void removeAmount(IToolStackView tool, ModifierEntry modifier, int amount) {
    tool.setDamage(tool.getDamage() + amount);
  }
}
