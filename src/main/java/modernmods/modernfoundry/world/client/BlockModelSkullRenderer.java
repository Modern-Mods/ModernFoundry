package modernmods.modernfoundry.world.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Skull model instance for the sake of making a Slimeskull with a block item
 * Requires {@link net.minecraft.world.inventory.InventoryMenu#BLOCK_ATLAS} as the texture for the skull.
 **/
// 26.1.2: client render overhaul — the old implementation rendered a block ItemStack as a skull via
// ItemRenderer#getModel/renderModelLists + BakedModel, all of which were removed in favor of the
// ItemStackRenderState / ItemModelResolver + submit pipeline. SkullModelBase is now Model<State> with a
// final renderToBuffer, so this is a minimal correct-shaped stub: it constructs (empty root part) and
// carries the stack/resolver for a later render pass to drive the item render state. Constructor now takes
// an ItemModelResolver (was ItemRenderer) — SlimeskullArmorModel must call getItemModelResolver().
public class BlockModelSkullRenderer extends SkullModelBase {
  private final ItemModelResolver itemModelResolver;
  private final ItemStack stack;

  public BlockModelSkullRenderer(ItemModelResolver itemModelResolver, ItemStack stack) {
    super(new ModelPart(List.of(), Map.of()));
    this.itemModelResolver = itemModelResolver;
    this.stack = stack;
  }

  @Override
  public void setupAnim(State state) {
    // rotation handled by the new submit-based skull render pass
  }
}
