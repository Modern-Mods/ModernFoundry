package modernmods.modernfoundry.library.client.book.sectiontransformer;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import modernmods.mantle.client.book.data.content.PageContent;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.book.content.ContentModifier;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierManager;

import java.util.Iterator;

/** Injects modifiers into a section based on a tag */
public class ModifierTagInjectorTransformer extends AbstractTagInjectingTransformer<Modifier> {
  public static final ModifierTagInjectorTransformer INSTANCE = new ModifierTagInjectorTransformer();

  private ModifierTagInjectorTransformer() {
    super(ModifierManager.REGISTRY_KEY, TConstruct.getResource("load_modifiers"), ContentModifier.ID);
  }

  @Override
  protected Iterator<Modifier> getTagEntries(TagKey<Modifier> tag) {
    return ModifierManager.getTagValues(tag).iterator();
  }

  @Override
  protected Identifier getId(Modifier modifier) {
    return modifier.getId().getIdentifier();
  }

  @Override
  protected PageContent createFallback(Modifier modifier) {
    return new ContentModifier(modifier);
  }
}
