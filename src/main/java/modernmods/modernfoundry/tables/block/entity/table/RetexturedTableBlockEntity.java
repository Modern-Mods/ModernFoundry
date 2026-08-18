package modernmods.modernfoundry.tables.block.entity.table;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.model.data.ModelData;
import modernmods.mantle.block.entity.IRetexturedBlockEntity;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.shared.block.entity.TableBlockEntity;

import javax.annotation.Nonnull;

public abstract class RetexturedTableBlockEntity extends TableBlockEntity implements IRetexturedBlockEntity {
  private static final String TAG_TEXTURE = "texture";

  @Nonnull @Getter
  protected Block texture = Blocks.AIR;
  public RetexturedTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name, int size) {
    super(type, pos, state, name, size);
  }
  public AABB getRenderBoundingBox() {
    return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1);
  }


  /* Textures */

  @Nonnull
  @Override
  public ModelData getModelData() {
    return RetexturedHelper.getModelData(texture);
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  private void textureUpdated() {
    // update the texture in BE data
    if (level != null && level.isClientSide()) {
      Block normalizedTexture = texture == Blocks.AIR ? null : texture;
      ModelData data = getModelData();
      if (data.get(RetexturedHelper.BLOCK_PROPERTY) != normalizedTexture) {
        requestModelDataUpdate();
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 0);
      }
    }
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = texture;
    texture = RetexturedHelper.getBlock(name);
    if (oldTexture != texture) {
      setChangedFast();
      textureUpdated();
    }
  }

  @Override
  public void saveSynced(ValueOutput output) {
    super.saveSynced(output);
    if (texture != Blocks.AIR) {
      output.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    String tex = input.getStringOr(TAG_TEXTURE, "");
    if (!tex.isEmpty()) {
      texture = RetexturedHelper.getBlock(tex);
      textureUpdated();
    }
  }
}
