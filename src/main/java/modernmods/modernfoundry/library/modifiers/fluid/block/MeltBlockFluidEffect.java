package modernmods.modernfoundry.library.modifiers.fluid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.block.BlockPredicate;
import modernmods.modernfoundry.library.modifiers.fluid.EffectLevel;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectContext;
import modernmods.modernfoundry.library.recipe.melting.MeltingRecipeLookup;

/**
 * Fluid effect that melts the targeted block into a fluid block
 * @param validBlocks  Blocks that may be melted
 * @param minAmount    Minimum amount of fluid it must melt into to be considered
 * @param temperature  Temperature needed to melt the block
 */
public record MeltBlockFluidEffect(IJsonPredicate<BlockState> validBlocks, int minAmount, int temperature) implements FluidEffect<FluidEffectContext.Block> {
  public static final RecordLoadable<MeltBlockFluidEffect> LOADER = RecordLoadable.create(
    BlockPredicate.LOADER.defaultField("blocks", MeltBlockFluidEffect::validBlocks),
    IntLoadable.FROM_ONE.requiredField("min_amount", MeltBlockFluidEffect::minAmount),
    IntLoadable.FROM_ONE.requiredField("temperature", MeltBlockFluidEffect::temperature),
    MeltBlockFluidEffect::new);

  @Override
  public RecordLoadable<MeltBlockFluidEffect> getLoader() {
    return LOADER;
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, FluidEffectContext.Block context, FluidAction action) {
    // no air, otherwise must be valid and used all the fluid (we don't have granularity here)
    BlockState state = context.getBlockState();
    if (state.isAir() || !validBlocks.matches(state) || !level.isFull()) {
      return 0;
    }

    Level world = context.getLevel();
    BlockPos pos = context.getBlockPos();
    float requirement = state.getDestroySpeed(world, pos);
    // check unbreakable
    if (requirement < 0 || context.breakRestricted()) {
      return 0;
    }

    // ensure it melts, don't break if it doesn't melt
    Item item = state.getBlock().asItem();
    if (item == Items.AIR) {
      return 0;
    }
    FluidStack result = MeltingRecipeLookup.findResult(item, temperature);
    if (result.isEmpty()) {
      return 0;
    }

    // from this point on the block will melt, just a question of whether it leaves fluid behind or air
    if (action.execute() && !world.isClientSide()) {
      // if we don't have enough and its not flowing, nothing much to do
      BlockState toPlace = Blocks.AIR.defaultBlockState();
      // have enough? place full block
      if (result.getAmount() >= minAmount) {
        toPlace = result.getFluid().defaultFluidState().createLegacyBlock();
      } else if (result.getFluid() instanceof FlowingFluid flowing) {
        // place a block that will disappear in a few ticks
        int fluidLevel = result.getAmount() * 8 / minAmount;
        if (fluidLevel > 0) {
          toPlace = flowing.getFlowing(fluidLevel, false).createLegacyBlock();
        }
      }
      // update block and send particles
      if (world.setBlockAndUpdate(pos, toPlace)) {
        world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
      }
    }

    return 1;
  }
}
