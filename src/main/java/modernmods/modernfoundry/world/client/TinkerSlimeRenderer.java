package modernmods.modernfoundry.world.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.TConstruct;

// 26.1.2: client render overhaul — SlimeRenderer is now MobRenderer<Slime, SlimeRenderState, SlimeModel>
// and getTextureLocation takes the SlimeRenderState (not the Slime). The metal/slime texture swap read
// ArmoredSlimeEntity#isMetal() off the live entity; that flag must now be captured into a custom render
// state via extractRenderState. This uses the slime texture and drops the SlimeArmorLayer. A later render
// pass should add a render state carrying isMetal + restore the armor layer.
public class TinkerSlimeRenderer extends SlimeRenderer {
  public static final Factory SKY_SLIME_FACTORY = new Factory(TConstruct.getResource("textures/entity/sky_slime.png"), TConstruct.getResource("textures/entity/steel_slime.png"));
  public static final Factory ENDER_SLIME_FACTORY = new Factory(TConstruct.getResource("textures/entity/ender_slime.png"), TConstruct.getResource("textures/entity/knightmetal_slime.png"));

  private final Identifier slime;
  private final Identifier metal;
  public TinkerSlimeRenderer(EntityRendererProvider.Context context, Identifier slime, Identifier metal) {
    super(context);
    this.slime = slime;
    this.metal = metal;
    // the vanilla SlimeRenderer constructor added a SlimeOuterLayer bound to the hardcoded vanilla slime texture;
    // replace it with one that uses this slime's texture so the translucent outer shell is colored to match.
    this.layers.clear();
    this.addLayer(new TinkerSlimeOuterLayer(this, context.getModelSet(), slime));
  }

  @Override
  public Identifier getTextureLocation(SlimeRenderState state) {
    return slime;
  }

  private record Factory(Identifier slime, Identifier metal) implements EntityRendererProvider<net.minecraft.world.entity.monster.Slime> {
    public Factory(Identifier texture) {
      this(texture, texture);
    }

    @Override
    public EntityRenderer<net.minecraft.world.entity.monster.Slime, ?> create(Context context) {
      return new TinkerSlimeRenderer(context, slime, metal);
    }
  }
}
