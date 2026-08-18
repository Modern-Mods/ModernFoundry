package modernmods.modernfoundry.library.client.book.content;

import net.minecraft.resources.Identifier;
import modernmods.mantle.client.book.data.BookData;
import modernmods.mantle.client.screen.book.element.ItemElement;
import modernmods.mantle.util.html.HtmlElement;
import modernmods.mantle.util.html.HtmlSerializable;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.tools.TinkerTools;
import modernmods.modernfoundry.tools.stats.HandleMaterialStats;
import modernmods.modernfoundry.tools.stats.HeadMaterialStats;
import modernmods.modernfoundry.tools.stats.StatlessMaterialStats;

import javax.annotation.Nullable;
import java.util.List;

import static modernmods.modernfoundry.TConstruct.getResource;

/**
 * Content page for melee/harvest materials
 */
public class MeleeHarvestMaterialContent extends AbstractMaterialContent {
  /** Page ID for using this index directly */
  public static final Identifier ID = getResource("melee_harvest_material");

  public MeleeHarvestMaterialContent(MaterialVariantId materialVariant, boolean detailed) {
    super(materialVariant, detailed);
  }

  @Override
  public Identifier getId() {
    return ID;
  }

  @Nullable
  @Override
  protected MaterialStatsId getStatType(int index) {
    return switch (index) {
      case 0 -> HeadMaterialStats.ID;
      case 1 -> HandleMaterialStats.ID;
      case 2 -> StatlessMaterialStats.BINDING.getIdentifier();
      default -> null;
    };
  }

  @Override
  protected String getTextKey(MaterialId material) {
    return String.format(detailed ? "material.%s.%s.encyclopedia" : "material.%s.%s.flavor", material.getNamespace(), material.getPath());
  }

  @Override
  protected boolean supportsStatType(MaterialStatsId statsId) {
    return statsId.equals(HeadMaterialStats.ID) || statsId.equals(HandleMaterialStats.ID) || statsId.equals(StatlessMaterialStats.BINDING.getIdentifier());
  }


  /* Categories */

  @Override
  protected void addCategory(List<ItemElement> displayTools, MaterialId material) {
    if (MaterialRegistry.getInstance().isInTag(material, TinkerTags.Materials.GENERAL)) {
      displayTools.add(makeCategoryIcon(TinkerTools.handAxe.get().getRenderTool(), getResource("general")));
    } else if (MaterialRegistry.getInstance().isInTag(material, TinkerTags.Materials.MELEE)) {
      displayTools.add(makeCategoryIcon(TinkerTools.sword.get().getRenderTool(),   getResource("melee")));
    } else if (MaterialRegistry.getInstance().isInTag(material, TinkerTags.Materials.HARVEST)) {
      displayTools.add(makeCategoryIcon(TinkerTools.pickaxe.get().getRenderTool(), getResource("harvest")));
    }
  }

  @Override
  protected HtmlSerializable makeStatsHtml(BookData data) {
    return HtmlElement.div().classes("row-material-stats")
      .add(HtmlElement.div().classes("column")
        .add(makeStatHtml(HeadMaterialStats.ID))
        .add(makeStatHtml(StatlessMaterialStats.BINDING.getIdentifier())))
      .add(makeStatHtml(HandleMaterialStats.ID));
  }
}
