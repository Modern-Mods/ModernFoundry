package modernmods.modernfoundry.library.data.tinkering;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import modernmods.modernfoundry.library.data.AbstractTagProvider;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierManager;

/** Tag provider to generate modifier tags */
public abstract class AbstractModifierTagProvider extends AbstractTagProvider<Modifier> {
  protected AbstractModifierTagProvider(PackOutput packOutput, String modId, ExistingFileHelper existingFileHelper) {
    // TODO: we don't fire modifier event during datagen, should we?
    super(packOutput, modId, ModifierManager.TAG_FOLDER, Modifier::getId, id -> true/*ModifierManager.INSTANCE.containsStatic(new ModifierId(id))*/, existingFileHelper);
  }
}
