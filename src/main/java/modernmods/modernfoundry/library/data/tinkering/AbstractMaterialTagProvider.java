package modernmods.modernfoundry.library.data.tinkering;

import net.minecraft.data.PackOutput;
import modernmods.modernfoundry.library.data.AbstractTagProvider;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialManager;

/** Tag provider for materials */
public abstract class AbstractMaterialTagProvider extends AbstractTagProvider<IMaterial> {
  protected AbstractMaterialTagProvider(PackOutput packOutput, String modId) {
    super(packOutput, modId, MaterialManager.TAG_FOLDER, m -> m.getIdentifier().getIdentifier(), id -> true);
  }
}
