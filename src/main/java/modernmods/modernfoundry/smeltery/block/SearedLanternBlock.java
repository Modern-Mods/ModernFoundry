package modernmods.modernfoundry.smeltery.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.HitResult;
import modernmods.mantle.util.BlockEntityHelper;
import modernmods.modernfoundry.library.utils.NBTTags;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.smeltery.block.component.SearedTankBlock;
import modernmods.modernfoundry.smeltery.block.entity.ITankBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.LanternBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.component.TankBlockEntity;
import modernmods.modernfoundry.smeltery.block.entity.component.TankBlockEntity.ITankBlock;

import javax.annotation.Nullable;

import static modernmods.modernfoundry.smeltery.block.component.SearedTankBlock.LIGHT;

public class SearedLanternBlock extends LanternBlock implements ITankBlock, EntityBlock {
  @Getter
  private final int capacity;
  public SearedLanternBlock(Properties properties, int capacity) {
    super(properties);
    this.capacity = capacity;
    registerDefaultState(defaultBlockState().setValue(LIGHT, 0));
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(LIGHT);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new LanternBlockEntity(pos, state, this);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState state = super.getStateForPlacement(context);
    if (state != null) {
      return SearedTankBlock.setLightLevel(state, context);
    }
    return null;
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    CompoundTag nbt = TagUtil.getTag(stack);
    if (nbt != null && world.getBlockEntity(pos) instanceof TankBlockEntity tank) {
      tank.updateTank(nbt.getCompoundOrEmpty(NBTTags.TANK));
    }
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, net.minecraft.core.Direction direction) {
    return ITankBlockEntity.getComparatorInputOverride(worldIn, pos);
  }

  @Override
  public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
    ItemStack stack = new ItemStack(this);
    BlockEntityHelper.get(TankBlockEntity.class, world, pos).ifPresent(te -> te.setTankTag(stack));
    return stack;
  }
}
