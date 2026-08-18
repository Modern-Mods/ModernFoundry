package modernmods.modernfoundry.tables.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.tables.block.entity.table.TinkerStationBlockEntity;

import javax.annotation.Nullable;

public class TinkerStationBlock extends RetexturedTableBlock {
  @Getter
  private final int slotCount;

  public TinkerStationBlock(Properties builder, int slotCount) {
    super(builder);
    this.slotCount = slotCount;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new TinkerStationBlockEntity(pPos, pState, getSlotCount());
  }


  /* Material texture */

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    if (TagUtil.hasTag(stack) && level.getBlockEntity(pos) instanceof TinkerStationBlockEntity be) {
      // try block first
      String block = RetexturedHelper.getTextureName(stack);
      if (!block.isEmpty()) {
        be.updateTexture(block);
      } else {
        // if no block, try material
        MaterialVariantId material = IMaterialItem.getMaterialFromStack(stack);
        if (material != IMaterial.UNKNOWN_ID) {
          be.setMaterial(material);
        }
      }
    }
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
    ItemStack stack = new ItemStack(state.getBlock());
    if (level.getBlockEntity(pos) instanceof TinkerStationBlockEntity be) {
      Block block = be.getTexture();
      if (block != Blocks.AIR) {
        RetexturedHelper.setTexture(stack, block);
      } else {
        stack = IMaterialItem.withMaterial(stack, be.getMaterial());
      }
    }
    return stack;
  }
}
