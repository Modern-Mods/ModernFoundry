package modernmods.modernfoundry.library.client.book.content;

import net.minecraft.resources.ResourceLocation;
import modernmods.hilt.client.book.data.BookData;
import modernmods.hilt.client.screen.book.element.ItemElement;
import modernmods.hilt.util.html.HtmlElement;
import modernmods.hilt.util.html.HtmlSerializable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.tools.TinkerTools;
import modernmods.modernfoundry.tools.stats.GripMaterialStats;
import modernmods.modernfoundry.tools.stats.LimbMaterialStats;
import modernmods.modernfoundry.tools.stats.StatlessMaterialStats;

import javax.annotation.Nullable;
import java.util.List;

import static modernmods.modernfoundry.TConstruct.getResource;

public class RangedMaterialContent extends AbstractMaterialContent {
  /** Page ID for using this index directly */
  public static final ResourceLocation ID = TConstruct.getResource("ranged_material");

  public RangedMaterialContent(MaterialVariantId materialVariant, boolean detailed) {
    super(materialVariant, detailed);
  }

  @Override
  public ResourceLocation getId() {
    return ID;
  }

  @Nullable
  @Override
  protected MaterialStatsId getStatType(int index) {
    return switch (index) {
      case 0 -> LimbMaterialStats.ID;
      case 1 -> GripMaterialStats.ID;
      case 2 -> StatlessMaterialStats.BOWSTRING.getIdentifier();
      default -> null;
    };
  }

  @Override
  protected String getTextKey(MaterialId material) {
    if (detailed) {
      String primaryKey = String.format("material.%s.%s.ranged", material.getNamespace(), material.getPath());
      if (Util.canTranslate(primaryKey)) {
        return primaryKey;
      }
      return String.format("material.%s.%s.encyclopedia", material.getNamespace(), material.getPath());
    }
    return String.format("material.%s.%s.flavor", material.getNamespace(), material.getPath());
  }

  @Override
  protected boolean supportsStatType(MaterialStatsId statsId) {
    return statsId.equals(LimbMaterialStats.ID) || statsId.equals(GripMaterialStats.ID) || statsId.equals(StatlessMaterialStats.BOWSTRING.getIdentifier());
  }


  /* Categories */

  @Override
  protected void addCategory(List<ItemElement> displayTools, MaterialId material) {
    if (MaterialRegistry.getInstance().isInTag(material, TinkerTags.Materials.BALANCED)) {
      displayTools.add(makeCategoryIcon(TinkerTools.fishingRod.get().getRenderTool(), getResource("balanced")));
    } else if (MaterialRegistry.getInstance().isInTag(material, TinkerTags.Materials.LIGHT)) {
      displayTools.add(makeCategoryIcon(TinkerTools.crossbow.get().getRenderTool(), getResource("light")));
    } else if (MaterialRegistry.getInstance().isInTag(material, TinkerTags.Materials.HEAVY)) {
      displayTools.add(makeCategoryIcon(TinkerTools.longbow.get().getRenderTool(), getResource("heavy")));
    }
  }

  @Override
  protected HtmlSerializable makeStatsHtml(BookData data) {
    return HtmlElement.div().classes("row-material-stats")
      .add(HtmlElement.div().classes("column")
        .add(makeStatHtml(LimbMaterialStats.ID))
        .add(makeStatHtml(StatlessMaterialStats.BOWSTRING.getIdentifier())))
      .add(makeStatHtml(GripMaterialStats.ID));
  }
}
