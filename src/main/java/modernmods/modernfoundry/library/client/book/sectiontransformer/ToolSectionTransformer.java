package modernmods.modernfoundry.library.client.book.sectiontransformer;

import net.minecraft.world.item.Items;
import modernmods.hilt.client.book.data.BookData;
import modernmods.hilt.client.book.data.PageData;
import modernmods.hilt.client.book.transformer.ContentGroupingSectionTransformer;
import modernmods.modernfoundry.library.client.book.content.ContentTool;

/** Section transformer to generate an index with tool names */
public class ToolSectionTransformer extends ContentGroupingSectionTransformer {
  public static final ToolSectionTransformer INSTANCE = new ToolSectionTransformer("tools");

  public ToolSectionTransformer(String name, boolean largeTitle, boolean centerTitle) {
    super(name, largeTitle, centerTitle);
  }

  public ToolSectionTransformer(String name) {
    super(name, null, null);
  }

  @Override
  protected boolean processPage(BookData book, GroupingBuilder builder, PageData page) {
    // only add tool pages if the tool exists, barrier is the fallback item for missing
    if (page.content instanceof ContentTool tool) {
      if (tool.getTool().asItem() != Items.BARRIER) {
        builder.addPage(page.getTitle(), page);
        return true;
      }
      return false;
    } else if (page.name.startsWith("group_")) {
      // skip adding the page if no data
      if (page.data.isEmpty() || "no-load".equals(page.data)) {
        builder.addGroup(page.getTitle(), null);
        return false;
      } else {
        builder.addGroup(page.getTitle(), page);
        return true;
      }
    // anything other than hidden continues same column
    } else if (!page.name.equals("hidden")) {
      builder.addPage(page.getTitle(), page);
    }
    return true;
  }
}
