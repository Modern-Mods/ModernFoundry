package modernmods.modernfoundry.fluids.fluids;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import modernmods.modernfoundry.library.utils.TagUtil;

/**
 * Fluid tint source reproducing the legacy potion NBT tint logic.
 * <p>
 * In 26.1 the {@code IClientFluidTypeExtensions#getTintColor(FluidStack)} overload was removed; a fluid's per-stack tint
 * is now supplied by a {@link FluidTintSource} attached to the fluid's {@link net.minecraft.client.renderer.block.FluidModel}
 * (see {@code FluidClientEvents#registerFluidModels}). This resolves the color from the stored potion data via
 * {@link #colorAsStack(FluidStack)}; the plain {@link #color(FluidState)} in-world path has no NBT so it falls back to the
 * base (water) potion color.
 */
public class PotionFluidTintSource implements FluidTintSource {
  public static final PotionFluidTintSource INSTANCE = new PotionFluidTintSource();

  /** Base color for the empty/water potion, used when no potion effects are present or in the block/world context. */
  private static final int WATER_COLOR = 0xFF000000 | (PotionUtils.getColor(Potions.WATER) & 0xFFFFFF);

  private PotionFluidTintSource() {}

  @Override
  public int color(FluidState state) {
    // block/world fluid rendering has no potion NBT available, so use the base potion color
    return WATER_COLOR;
  }

  @Override
  public int colorAsStack(FluidStack stack) {
    CompoundTag tag = TagUtil.getTag(stack);
    if (tag != null && tag.contains("CustomPotionColor")) {
      return tag.getInt("CustomPotionColor").orElse(0) | 0xFF000000;
    }
    if (PotionUtils.getPotion(tag).is(Potions.WATER)) {
      return WATER_COLOR;
    }
    return PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xFF000000;
  }
}
