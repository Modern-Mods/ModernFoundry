package modernmods.modernfoundry.library.modifiers.modules.build;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.hilt.data.loadable.field.LegacyField;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.VolatileDataModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModuleBuilder;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.SlotType;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.ToolDataNBT;

import java.util.List;

/**
 * Module that adds extra modifier slots to a tool.
 */
public record ModifierSlotModule(SlotType type, LevelingInt count, ModifierCondition<IToolContext> condition) implements VolatileDataModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierSlotModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<ModifierSlotModule> LOADER = RecordLoadable.create(
    SlotType.LOADABLE.requiredField("name", ModifierSlotModule::type),
    IntLoadable.ANY_SHORT.defaultField("flat", 0, m -> m.count.flat()),
    // TODO 1.21ish: drop legacy support in favor of a direct LevelingInt field
    new LegacyField<>(IntLoadable.ANY_SHORT.defaultField("each_level", 0, m -> m.count.eachLevel()), "count"),
    ModifierCondition.CONTEXT_FIELD,
    (type, flat, leveling, condition) -> new ModifierSlotModule(type, new LevelingInt(flat, leveling), condition));
  
  /** @apiNote Internal constructor, use {@link #slot(SlotType)} */
  @Internal
  public ModifierSlotModule {}

  /** @deprecated use {@link #slot(SlotType)} */
  @Deprecated(forRemoval = true)
  public ModifierSlotModule(SlotType type, int count, ModifierCondition<IToolContext> condition) {
    this(type, LevelingInt.eachLevel(count), condition);
  }

  /** @deprecated use {@link #slot(SlotType)} */
  @Deprecated(forRemoval = true)
  public ModifierSlotModule(SlotType type, int count) {
    this(type, count, ModifierCondition.ANY_CONTEXT);
  }

  /** @deprecated use {@link #slot(SlotType)} */
  @Deprecated(forRemoval = true)
  public ModifierSlotModule(SlotType type) {
    this(type, 1);
  }

  @Override
  public Integer getPriority() {
    // show lower priority so they group together
    return 50;
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.addSlots(type, count.computeForLevel(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public RecordLoadable<ModifierSlotModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  
  /** Creates a builder instance */
  public static Builder slot(SlotType slotType) {
    return new Builder(slotType);
  }
  
  /** Builder for the module */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends ModuleBuilder.Context<Builder> implements LevelingInt.Builder<ModifierSlotModule> {
    private final SlotType slotType;

    @Override
    public ModifierSlotModule amount(int flat, int eachLevel) {
      return new ModifierSlotModule(slotType, new LevelingInt(flat, eachLevel), condition);
    }
  }
}
