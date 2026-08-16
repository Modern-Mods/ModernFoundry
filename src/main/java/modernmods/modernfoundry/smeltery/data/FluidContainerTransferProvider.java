package modernmods.modernfoundry.smeltery.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ItemExistsCondition;
import modernmods.hilt.fluid.transfer.AbstractFluidContainerTransferProvider;
import modernmods.hilt.fluid.transfer.EmptyFluidContainerTransfer;
import modernmods.hilt.fluid.transfer.FillFluidContainerTransfer;
import modernmods.hilt.recipe.data.ItemNameIngredient;
import modernmods.hilt.recipe.helper.FluidOutput;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.registration.object.FluidObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.fluids.TinkerFluids;
import modernmods.modernfoundry.library.recipe.FluidValues;
import modernmods.modernfoundry.shared.block.SlimeType;

public class FluidContainerTransferProvider extends AbstractFluidContainerTransferProvider {
  public FluidContainerTransferProvider(PackOutput packOutput) {
    super(packOutput, TConstruct.MOD_ID);
  }

  @Override
  protected void addTransfers() {
    addFillEmpty("meat_soup_", TinkerFluids.meatSoupBowl, Items.BOWL, TinkerFluids.meatSoup, FluidValues.BOWL, false);
    // these bottles are fluid handlers, but glass bottles are not
    addBottleFill("venom_bottle_fill", TinkerFluids.venomBottle, TinkerFluids.venom);
    addBottleFill("earth_slime_bottle_fill", TinkerFluids.slimeBottle.get(SlimeType.EARTH), TinkerFluids.earthSlime);
    addBottleFill("sky_slime_bottle_fill",   TinkerFluids.slimeBottle.get(SlimeType.SKY),   TinkerFluids.skySlime);
    addBottleFill("ichor_slime_bottle_fill", TinkerFluids.slimeBottle.get(SlimeType.ICHOR), TinkerFluids.ichor);
    addBottleFill("ender_slime_bottle_fill", TinkerFluids.slimeBottle.get(SlimeType.ENDER), TinkerFluids.enderSlime);
    addBottleFill("magma_bottle_fill",       TinkerFluids.magmaBottle,                      TinkerFluids.magma);

    // fiery bottles
    String tf = "twilightforest";
    FluidOutput fieryBottle = TinkerFluids.fieryLiquid.result(FluidValues.BOTTLE);
    addContainerlessEmpty("fiery_blood", tf, fieryBottle);
    addContainerlessEmpty("fiery_tears", tf, fieryBottle);
  }

  /** Adds a recipe for a bottle that fills with 250mb of fluid, emptying is assumed handled */
  protected void addBottleFill(String name, ItemLike output, FluidObject<?> fluid) {
    addTransfer(name, new FillFluidContainerTransfer(Ingredient.of(Items.GLASS_BOTTLE), ItemOutput.fromItem(output), fluid.ingredient(FluidValues.BOTTLE)));
  }

  /** Adds a recipe to empty an item, returning no container */
  @SuppressWarnings("removal")
  protected void addContainerlessEmpty(String name, String domain, FluidOutput fluid) {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(domain, name);
    addTransfer(domain + '_' + name, new EmptyFluidContainerTransfer(ItemNameIngredient.from(id), ItemOutput.EMPTY, fluid), new ItemExistsCondition(id));
  }

  @Override
  public String getName() {
    return "Modern Foundry Fluid Container Transfer";
  }
}
