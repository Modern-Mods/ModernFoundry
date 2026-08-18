package modernmods.modernfoundry.library.json.predicate;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.modernfoundry.compat.neoforged.neoforge.common.TierSortingRegistry;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.block.BlockPredicate;
import modernmods.modernfoundry.library.json.TinkerLoadables;

/** Block predicate matching anything minable by the given tier */
public record HarvestTierPredicate(ToolMaterial tier) implements BlockPredicate {
  public static final RecordLoadable<HarvestTierPredicate> LOADER = RecordLoadable.create(TinkerLoadables.TIER.requiredField("tier", HarvestTierPredicate::tier), HarvestTierPredicate::new);

  @Override
  public boolean matches(BlockState state) {
    return TierSortingRegistry.isCorrectTierForDrops(tier, state);
  }

  @Override
  public RecordLoadable<? extends IJsonPredicate<BlockState>> getLoader() {
    return LOADER;
  }
}
