package modernmods.modernfoundry.library.tools.definition.module.build;

import net.minecraft.world.item.ArmorItem;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.MultiplierNBT;
import modernmods.modernfoundry.library.tools.nbt.StatsNBT;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.IToolStat;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.modules.ArmorModuleBuilder;

import java.util.List;

/** Module to set global multipliers on the tool */
public record MultiplyStatsModule(MultiplierNBT multipliers) implements ToolStatsHook, ToolModule {
  public static final RecordLoadable<MultiplyStatsModule> LOADER = RecordLoadable.create(MultiplierNBT.LOADABLE.requiredField("multipliers", MultiplyStatsModule::multipliers), MultiplyStatsModule::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MultiplyStatsModule>defaultHooks(ToolHooks.TOOL_STATS);

  @Override
  public RecordLoadable<MultiplyStatsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addToolStats(IToolContext context, ModifierStatsBuilder builder) {
    for (INumericToolStat<?> stat : multipliers.getContainedStats()) {
      stat.multiplyAll(builder, multipliers.get(stat));
    }
  }


  /** Creates a builder instance */
  public static ArmorBuilder armor(List<ArmorItem.Type> slots) {
    return new ArmorBuilder(slots);
  }

  public static class ArmorBuilder implements ArmorModuleBuilder<MultiplyStatsModule> {
    private final List<ArmorItem.Type> slotTypes;
    private final MultiplierNBT.Builder[] builders = new MultiplierNBT.Builder[4];

    private ArmorBuilder(List<ArmorItem.Type> slotTypes) {
      this.slotTypes = slotTypes;
      for (ArmorItem.Type slotType : slotTypes) {
        builders[slotType.ordinal()] = MultiplierNBT.builder();
      }
    }

    /** Gets the builder for the given slot */
    protected MultiplierNBT.Builder getBuilder(ArmorItem.Type slotType) {
      MultiplierNBT.Builder builder = builders[slotType.ordinal()];
      if (builder == null) {
        throw new IllegalArgumentException("Unsupported slot type " + slotType);
      }
      return builder;
    }

    /** Adds a bonus to the builder */
    public ArmorBuilder set(ArmorItem.Type slotType, INumericToolStat<?> stat, float value) {
      getBuilder(slotType).set(stat, value);
      return this;
    }

    /** Sets the same bonus on all pieces */
    public ArmorBuilder setAll(INumericToolStat<?> stat, float value) {
      for (ArmorItem.Type slotType : slotTypes) {
        set(slotType, stat, value);
      }
      return this;
    }

    /**
     * Sets a different bonus on all pieces.
     * order is usually helmet, chestplate, leggings, boot, but depends on the order from {@link SetStatsModule#armor(List)}
     */
    public final ArmorBuilder setInOrder(INumericToolStat<?> stat, float... values) {
      if (values.length != slotTypes.size()) {
        throw new IllegalStateException("Wrong number of stats set");
      }
      for (int i = 0; i < values.length; i++) {
        set(slotTypes.get(i), stat, values[i]);
      }
      return this;
    }

    /** Builds the final module */
    @Override
    public MultiplyStatsModule build(ArmorItem.Type slot) {
      return new MultiplyStatsModule(getBuilder(slot).build());
    }
  }
}
