package modernmods.modernfoundry.library.data.tinkering;

import net.minecraft.data.PackOutput;
import modernmods.modernfoundry.library.data.AbstractTagProvider;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierManager;

/** Tag provider to generate modifier tags */
public abstract class AbstractModifierTagProvider extends AbstractTagProvider<Modifier> {
  protected AbstractModifierTagProvider(PackOutput packOutput, String modId) {
    // TODO: we don't fire modifier event during datagen, should we?
    super(packOutput, modId, ModifierManager.TAG_FOLDER, m -> m.getId().getIdentifier(), id -> true/*ModifierManager.INSTANCE.containsStatic(new ModifierId(id))*/);
  }
}
