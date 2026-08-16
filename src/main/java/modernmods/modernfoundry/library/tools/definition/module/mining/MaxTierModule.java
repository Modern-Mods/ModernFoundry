package modernmods.modernfoundry.library.tools.definition.module.mining;

import net.minecraft.world.item.Tier;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.ToolModule;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.HarvestTiers;

import java.util.List;

/**
 * Module that limits the tier to the given max
 */
public record MaxTierModule(Tier tier) implements MiningTierToolHook, ToolModule {
  public static final RecordLoadable<MaxTierModule> LOADER = RecordLoadable.create(TinkerLoadables.TIER.requiredField("tier", MaxTierModule::tier), MaxTierModule::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaxTierModule>defaultHooks(ToolHooks.MINING_TIER);

  @Override
  public RecordLoadable<MaxTierModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Tier modifyTier(IToolStackView tool, Tier tier) {
    return HarvestTiers.min(this.tier, tier);
  }
}
