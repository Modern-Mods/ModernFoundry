package modernmods.modernfoundry.library.tools.definition.module.mining;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.block.BlockPredicate;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module for making tool break the given predicate in one click without instant breaking */
public record OneClickBreakModule(IJsonPredicate<BlockState> predicate) implements MiningSpeedToolHook, ToolModule {
  public static final RecordLoadable<OneClickBreakModule> LOADER = RecordLoadable.create(BlockPredicate.LOADER.directField("predicate_type", OneClickBreakModule::predicate), OneClickBreakModule::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<OneClickBreakModule>defaultHooks(ToolHooks.MINING_SPEED);

  /** Modifies the given tag */
  public static OneClickBreakModule tag(TagKey<Block> tag) {
    return new OneClickBreakModule(BlockPredicate.tag(tag));
  }

  @Override
  public RecordLoadable<OneClickBreakModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float modifyDestroySpeed(IToolStackView tool, BlockState state, float speed) {
    if (predicate.matches(state)) {
      speed = state.getBlock().defaultDestroyTime() * 20;
    }
    return speed;
  }
}
