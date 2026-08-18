package modernmods.modernfoundry.library.json.predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.block.BlockPredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;

/** @deprecated use {@link modernmods.mantle.data.predicate.entity.BlockAtEntityPredicate} */
@Deprecated
public record BlockAtFeetEntityPredicate(IJsonPredicate<BlockState> block) implements LivingEntityPredicate {
  public static final RecordLoadable<BlockAtFeetEntityPredicate> LOADER = RecordLoadable.create(BlockPredicate.LOADER.directField("block_type", BlockAtFeetEntityPredicate::block), BlockAtFeetEntityPredicate::new);

  @Override
  public RecordLoadable<BlockAtFeetEntityPredicate> getLoader() {
    return LOADER;
  }

  @Override
  public boolean matches(LivingEntity entity) {
    return block.matches(entity.level().getBlockState(entity.blockPosition()));
  }
}
