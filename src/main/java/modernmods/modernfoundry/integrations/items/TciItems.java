package modernmods.modernfoundry.integrations.items;

import modernmods.hilt.registration.object.FlowingFluidObject;
import modernmods.hilt.registration.object.MetalItemObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.registration.FluidDeferredRegisterExtension;
import modernmods.modernfoundry.integrations.common.TciModule;
import modernmods.modernfoundry.fluids.block.BurningLiquidBlock;
import modernmods.modernfoundry.world.TinkerWorld;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Function;

/** Native items, storage blocks, and molten fluids imported from TciIntegration. */
public final class TciItems extends TciModule {
  private static final FluidDeferredRegisterExtension FLUIDS = TciModule.FLUIDS;

  public static final MetalItemObject bronze = BLOCKS.registerMetal("bronze", metalBuilder(MapColor.WOOD), TOOLTIP_BLOCK_ITEM, ITEM_PROPS);

  public static final DeferredHolder<? super CreativeModeTab, CreativeModeTab> tabIntegrations = CREATIVE_TABS.register(
      "integrations", () -> CreativeModeTab.builder()
          .title(Component.translatable("itemGroup.modernfoundry.integrations"))
          .icon(() -> new ItemStack(bronze.getNugget()))
          .displayItems(TciItems::addTabItems)
          .withTabsBefore(TinkerWorld.tabWorld.getId())
          .build());

  /** Compatibility names retained for the translated data and client hooks. */
  public static final DeferredHolder<? super CreativeModeTab, CreativeModeTab> ITEM_TAB_GROUP = tabIntegrations;

  public static final FlowingFluidObject<BaseFlowingFluid> moltenManasteel = fluid("molten_manasteel", MapColor.RAW_IRON, 13, 1250, 5f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenNeptunium = fluid("molten_neptunium", MapColor.EMERALD, 14, 1250, 5f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenSourceGem = fluid("molten_source_gem", MapColor.COLOR_PURPLE, 14, 1280, 5f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenSoulStainedSteel = fluid("molten_soul_stained_steel", MapColor.COLOR_MAGENTA, 12, 1250, 5f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenCloggrum = fluid("molten_cloggrum", MapColor.TERRACOTTA_BROWN, 8, 1200, 5f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenFroststeel = fluid("molten_froststeel", MapColor.WATER, 11, 1200, 6f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenForgottenMetal = fluid("molten_forgotten_metal", MapColor.EMERALD, 14, 1200, 6f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenDesh = fluid("molten_desh", MapColor.TERRACOTTA_GREEN, 4, 800, 3f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenOstrum = fluid("molten_ostrum", MapColor.TERRACOTTA_PURPLE, 4, 800, 3f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenCalorite = fluid("molten_calorite", MapColor.TERRACOTTA_RED, 4, 800, 3f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenDragonsteelFire = fluid("molten_dragonsteel_fire", MapColor.TERRACOTTA_RED, 12, 1750, 5f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenDragonsteelIce = fluid("molten_dragonsteel_ice", MapColor.ICE, 11, 1750, 5f);
  public static final FlowingFluidObject<BaseFlowingFluid> moltenDragonsteelLightning = fluid("molten_dragonsteel_lightning", MapColor.TERRACOTTA_YELLOW, 14, 1750, 5f);

  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_MANASTEEL = moltenManasteel;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_NEPTUNIUM = moltenNeptunium;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_SOURCE_GEM = moltenSourceGem;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_SOUL_STAINED_STEEL = moltenSoulStainedSteel;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_CLOGGRUM = moltenCloggrum;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_FROSTSTEEL = moltenFroststeel;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_FORGOTTEN_METAL = moltenForgottenMetal;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_DESH = moltenDesh;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_OSTRUM = moltenOstrum;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_CALORITE = moltenCalorite;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_DRAGONSTEEL_FIRE = moltenDragonsteelFire;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_DRAGONSTEEL_ICE = moltenDragonsteelIce;
  public static final FlowingFluidObject<BaseFlowingFluid> MOLTEN_DRAGONSTEEL_LIGHTNING = moltenDragonsteelLightning;
  public static final MetalItemObject BRONZE = bronze;
  public static final Function<net.minecraft.world.level.block.Block, ?> GENERAL_TOOLTIP_BLOCK_ITEM = TOOLTIP_BLOCK_ITEM;

  private static FlowingFluidObject<BaseFlowingFluid> fluid(String name, MapColor color, int light, int temperature, float damage) {
    return FLUIDS.register(name).type(hot(name).temperature(temperature).lightLevel(light))
        .block(BurningLiquidBlock.createBurning(color, light, 10, damage)).bucket().commonTag().flowing();
  }

  private static FluidType.Properties hot(String name) {
    return FluidType.Properties.create().density(2000).viscosity(10000).temperature(1000)
        .descriptionId(TConstruct.makeDescriptionId("fluid", name))
        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
        .motionScale(0.0023333333333333335D).canSwim(false).canDrown(false)
        .pathType(PathType.LAVA).adjacentPathType(null);
  }

  private static void addTabItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
    output.accept(bronze.getNugget());
    output.accept(bronze.getIngot());
    output.accept(bronze.get());
    output.accept(moltenManasteel);
    output.accept(moltenNeptunium);
    output.accept(moltenSourceGem);
    output.accept(moltenSoulStainedSteel);
    output.accept(moltenCloggrum);
    output.accept(moltenFroststeel);
    output.accept(moltenForgottenMetal);
    output.accept(moltenDesh);
    output.accept(moltenOstrum);
    output.accept(moltenCalorite);
    output.accept(moltenDragonsteelFire);
    output.accept(moltenDragonsteelIce);
    output.accept(moltenDragonsteelLightning);
  }
}
