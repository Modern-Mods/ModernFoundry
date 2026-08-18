package modernmods.modernfoundry.world.client;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.modernfoundry.world.block.FoliageType;

import javax.annotation.Nullable;

/**
 * {@link BlockTintSource} for slime foliage (grass, leaves, ferns, vines). Colors tint index 0 by world position through
 * {@link SlimeColorizer}, replacing the pre-26.1 {@code BlockColors} position handler (the block color-handler event was
 * removed in the 26.1 render overhaul; block tints are now instances registered on
 * {@code RegisterColorHandlersEvent.BlockTintSources}).
 * <p>
 * Leaves and vines pass {@link SlimeColorizer#LOOP_OFFSET} so their color pattern is shifted relative to the grass they
 * sit on, matching the original mod.
 * @param type    Foliage type providing the colormap and the no-position fallback color
 * @param offset  Optional position offset applied before sampling (loop offset for leaves/vines), or null for grass
 */
public record SlimeFoliageTintSource(FoliageType type, @Nullable BlockPos offset) implements BlockTintSource {
  /** Position-independent fallback (e.g. inventory particle); uses the foliage type's flat color. */
  @Override
  public int color(BlockState state) {
    return 0xFF000000 | type.getColor();
  }

  @Override
  public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
    BlockPos target = offset != null ? pos.offset(offset) : pos;
    return 0xFF000000 | SlimeColorizer.getColorForPos(target, type);
  }
}
