package modernmods.modernfoundry.library.modifiers.modules.capacity;

import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.special.CapacityBarHook;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.OvergrowthModule;

/** Module with a chance to fill the capacity bar each second */
public class TimeToCapacityModule extends OvergrowthModule {
  public static final RecordLoadable<TimeToCapacityModule> LOADER = RecordLoadable.create(CHANCE_FIELD, ModifierCondition.TOOL_FIELD, TimeToCapacityModule::new);

  public TimeToCapacityModule(LevelingValue chance, ModifierCondition<IToolStackView> condition) {
    super(chance, condition);
  }

  @Override
  public RecordLoadable<TimeToCapacityModule> getLoader() {
    return LOADER;
  }

  @Override
  protected CapacityBarHook getBar(ModifierEntry modifier) {
    return modifier.getHook(ModifierHooks.CAPACITY_BAR);
  }
}
