package modernmods.modernfoundry.library.tools.part.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import modernmods.mantle.block.entity.MantleBlockEntity;
import modernmods.mantle.util.RetexturedHelper;
import modernmods.modernfoundry.library.client.model.ModelProperties;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.tools.TinkerToolParts;

import javax.annotation.Nonnull;
import java.util.Objects;

import static modernmods.modernfoundry.library.tools.part.IMaterialItem.MATERIAL_TAG;

/** Block entity logic for {@link MaterialBlock} */
public class MaterialBlockEntity extends MantleBlockEntity {
  @Nonnull
  @Getter
  private MaterialVariantId material = IMaterial.UNKNOWN_ID;

  /** Constructor for addons to register a new block entity for their material blocks */
  public MaterialBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  /** Constructor for our material blocks. */
  public MaterialBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerToolParts.materialBlock.get(), pos, state);
  }

  @Override
  public ModelData getModelData() {
    return ModelData.builder().with(ModelProperties.MATERIAL, material).build();
  }

  /** Called to update the material on the block. */
  public void setMaterial(MaterialVariantId material) {
    MaterialVariantId oldMaterial = this.material;
    // TODO: resolve redirects?
    this.material = material;
    if (!oldMaterial.equals(material)) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(net.minecraft.world.level.storage.ValueOutput output) {
    super.saveSynced(output);
    if (material != IMaterial.UNKNOWN_ID) {
      output.putString(MATERIAL_TAG, material.toString());
    }
  }

  @Override
  protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
    super.loadAdditional(input);
    String materialStr = input.getStringOr(MATERIAL_TAG, "");
    if (!materialStr.isEmpty()) {
      material = Objects.requireNonNullElse(MaterialVariantId.tryParse(materialStr), IMaterial.UNKNOWN_ID);
      RetexturedHelper.onTextureUpdated(this);
    }
  }
}
