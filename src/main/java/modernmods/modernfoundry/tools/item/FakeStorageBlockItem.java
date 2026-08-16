package modernmods.modernfoundry.tools.item;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import modernmods.modernfoundry.common.recipe.RecipeCacheInvalidator;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.tools.part.IRepairKitItem;
import modernmods.modernfoundry.library.tools.part.block.MaterialBlockItem;

import java.util.function.Predicate;

/** Simple implementation of a storage block for use with materials that lack blocks. */
public class FakeStorageBlockItem extends MaterialBlockItem implements IRepairKitItem {
  /** Cache of whether tag is present for each ingot */
  private final Object2BooleanArrayMap<MaterialId> missingItemCache = new Object2BooleanArrayMap<>();
  /** Getter to resolve the tag */
  private final Predicate<MaterialId> missingItemGetter = material -> FakeIngotItem.hasItem(material, getRepairAmount());

  private final int repairAmount;
  private final TagKey<IMaterial> validMaterials;
  public FakeStorageBlockItem(Block block, Properties properties, int repairAmount, TagKey<IMaterial> validMaterials) {
    super(block, properties);
    this.repairAmount = repairAmount;
    this.validMaterials = validMaterials;
    RecipeCacheInvalidator.addReloadListener(client -> missingItemCache.clear());
  }

  @Override
  public float getRepairAmount() {
    return repairAmount;
  }

  @Override
  public boolean canUseMaterial(MaterialId material) {
    return MaterialRegistry.getInstance().isInTag(material, validMaterials)
      && missingItemCache.computeIfAbsent(material, missingItemGetter);
  }

  @Override
  public boolean canRepairInCraftingTable() {
    return false;
  }
}
