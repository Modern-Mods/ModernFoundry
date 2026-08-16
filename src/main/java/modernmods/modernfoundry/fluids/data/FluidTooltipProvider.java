package modernmods.modernfoundry.fluids.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.fluids.FluidType;
import modernmods.hilt.fluid.tooltip.AbstractFluidTooltipProvider;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;
import modernmods.modernfoundry.smeltery.menu.AlloyerContainerMenu;
import modernmods.modernfoundry.smeltery.menu.MelterContainerMenu;

import static modernmods.modernfoundry.common.TinkerTags.Fluids.BOTTLE_TOOLTIPS;
import static modernmods.modernfoundry.common.TinkerTags.Fluids.CLAY_TOOLTIPS;
import static modernmods.modernfoundry.common.TinkerTags.Fluids.GLASS_TOOLTIPS;
import static modernmods.modernfoundry.common.TinkerTags.Fluids.LARGE_GEM_TOOLTIPS;
import static modernmods.modernfoundry.common.TinkerTags.Fluids.METAL_TOOLTIPS;
import static modernmods.modernfoundry.common.TinkerTags.Fluids.SLIME_TOOLTIPS;
import static modernmods.modernfoundry.common.TinkerTags.Fluids.SMALL_GEM_TOOLTIPS;

public class FluidTooltipProvider extends AbstractFluidTooltipProvider {
  public FluidTooltipProvider(PackOutput packOutput) {
    super(packOutput, TConstruct.MOD_ID);
  }

  @Override
  protected void addFluids() {
    // screen capacities
    add("ingots").addUnit("ingot", FluidValues.INGOT);
    addRedirect(AlloyerContainerMenu.TOOLTIP_FORMAT, id("ingots"));
    addRedirect(MelterContainerMenu.TOOLTIP_FORMAT, id("ingots"));
    addRedirect(TinkerSmeltery.smeltery.getId(), id("ingots"));
    addRedirect(TinkerSmeltery.foundry.getId(), id("ingots"));

    // standard fluids
    add("metals", METAL_TOOLTIPS)
      .addUnit("block", FluidValues.METAL_BLOCK)
      .addUnit("ingot", FluidValues.INGOT)
      .addUnit("nugget", FluidValues.NUGGET);
    add("large_gems", LARGE_GEM_TOOLTIPS)
      .addUnit("block", FluidValues.LARGE_GEM_BLOCK)
      .addUnit("gem", FluidValues.GEM)
      .addUnit("shard", FluidValues.GEM_SHARD);
    add("small_gems", SMALL_GEM_TOOLTIPS)
      .addUnit("block", FluidValues.SMALL_GEM_BLOCK)
      .addUnit("gem", FluidValues.GEM)
      .addUnit("shard", FluidValues.GEM_SHARD);

    add("clay", CLAY_TOOLTIPS)
      .addUnit("block", FluidValues.BRICK_BLOCK)
      .addUnit("brick", FluidValues.BRICK);
    add("slime", SLIME_TOOLTIPS)
      .addUnit("block", FluidValues.SLIME_BLOCK)
      .addUnit("slimeball", FluidValues.SLIMEBALL)
      .addUnit("drop", "hilt", FluidValues.SLIME_DROP);
    add("glass", GLASS_TOOLTIPS)
      .addUnit("block", FluidValues.GLASS_BLOCK)
      .addUnit("pane", FluidValues.GLASS_PANE);

    add("bottle", BOTTLE_TOOLTIPS)
      .addUnit("bucket", "hilt", FluidType.BUCKET_VOLUME)
      .addUnit("bottle", "hilt", FluidValues.BOTTLE)
      .addUnit("drop",   "hilt", FluidValues.SIP);
  }

  @Override
  public String getName() {
    return "Modern Foundry Fluid Tooltip Provider";
  }
}
