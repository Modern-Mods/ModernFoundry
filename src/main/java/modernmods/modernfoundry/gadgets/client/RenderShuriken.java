package modernmods.modernfoundry.gadgets.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import modernmods.modernfoundry.gadgets.entity.shuriken.ShurikenEntityBase;
import modernmods.modernfoundry.tools.client.material.ThrownShurikenRenderer;

/** @deprecated use {@link ThrownShurikenRenderer} */
@Deprecated(forRemoval = true)
public class RenderShuriken extends ThrownShurikenRenderer<ShurikenEntityBase> {
  public RenderShuriken(EntityRendererProvider.Context context) {
    super(context);
  }
}
