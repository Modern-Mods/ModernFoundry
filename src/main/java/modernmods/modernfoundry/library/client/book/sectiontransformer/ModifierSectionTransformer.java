package modernmods.modernfoundry.library.client.book.sectiontransformer;

import modernmods.hilt.client.book.data.BookData;
import modernmods.hilt.client.book.data.PageData;
import modernmods.hilt.client.book.transformer.ContentGroupingSectionTransformer;
import modernmods.modernfoundry.library.client.book.content.ContentModifier;
import modernmods.modernfoundry.library.modifiers.ModifierManager;

/** Section transformer to generate an index with modifier names */
public class ModifierSectionTransformer extends ContentGroupingSectionTransformer {
  public static final ModifierSectionTransformer INSTANCE = new ModifierSectionTransformer("modifiers");

  public ModifierSectionTransformer(String name, boolean largeTitle, boolean centerTitle) {
    super(name, largeTitle, centerTitle);
  }

  public ModifierSectionTransformer(String name) {
    super(name, null, null);
  }

  @Override
  protected boolean processPage(BookData book, GroupingBuilder builder, PageData page) {
    // modifiers add including their name
    if (page.content instanceof ContentModifier modifierContent) {
      if (modifierContent.getModifier() != ModifierManager.INSTANCE.getDefaultValue()) {
        builder.addPage(page.getTitle(), page);
        return true;
      }
      return false;
      // starting with group means start a new column
    } else if (page.name.startsWith("group_")) {
      // skip adding the page if no data
      if (page.data.isEmpty()) {
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
