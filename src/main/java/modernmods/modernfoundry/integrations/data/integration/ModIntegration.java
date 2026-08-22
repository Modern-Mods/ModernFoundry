package modernmods.modernfoundry.integrations.data.integration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.location;

/** Optional-mod identity and registry lookups used by the translated integrations. */
public final class ModIntegration {
  private ModIntegration() {}

  public static final String BOTANIA_MODID = "botania";
  public static final String IE_MODID = "immersiveengineering";
  public static final String TCON_MODID = "modernfoundry";
  public static final String CREATE_MODID = "create";
  public static final String AQUACULTURE_MODID = "aquaculture";
  public static final String ARS_MODID = "ars_nouveau";
  public static final String ALEX_MODID = "alexsmobs";
  public static final String MALUM_MODID = "malum";
  public static final String UNDERGARDEN_MODID = "undergarden";
  public static final String BEYOND_EARTH_MODID = "beyond_earth";
  public static final String MEKANISM_MODID = "mekanism";
  public static final String MYTHIC_BOTANY_MODID = "mythicbotany";
  public static final String IFD_MODID = "iceandfire";
  public static final String CONSECRATION_MODID = "consecration";
  public static final String AD_ASTRA_MODID = "ad_astra";
  public static final String APOTH_MODID = "apotheosis";
  public static final String ARS_ELEMENTAL_MODID = "ars_elemental";
  public static final String DEEPERDARKER_MODID = "deeperdarker";
  public static final String TWILIGHT_MODID = "twilightforest";

  /** This is refreshed after mod registries have finished loading. */
  public static Item BEYOND_EARTH_CHEESE = Items.AIR;

  public static void refresh() {
    BEYOND_EARTH_CHEESE = item(beyondEarthLoc("cheese"));
  }

  public static Item item(ResourceLocation id) {
    return BuiltInRegistries.ITEM.get(id);
  }

  public static boolean canLoad(String modid) {
    String dataGen = System.getenv("DATA_GEN");
    return (dataGen != null && dataGen.contains("all")) || ModList.get().isLoaded(modid);
  }

  public static ResourceLocation botaniaLoc(String name) { return location(BOTANIA_MODID, name); }
  public static ResourceLocation malumLoc(String name) { return location(MALUM_MODID, name); }
  public static ResourceLocation beyondEarthLoc(String name) { return location(BEYOND_EARTH_MODID, name); }
  public static ResourceLocation arsLoc(String name) { return location(ARS_MODID, name); }
  public static ResourceLocation ifdLoc(String name) { return location(IFD_MODID, name); }
  public static ResourceLocation mbotLoc(String name) { return location(MYTHIC_BOTANY_MODID, name); }
  public static ResourceLocation ieLoc(String name) { return location(IE_MODID, name); }
  public static ResourceLocation alexLoc(String name) { return location(ALEX_MODID, name); }
  public static ResourceLocation createLoc(String name) { return location(CREATE_MODID, name); }
  public static ResourceLocation aquaLoc(String name) { return location(AQUACULTURE_MODID, name); }
  public static ResourceLocation ugLoc(String name) { return location(UNDERGARDEN_MODID, name); }
  public static ResourceLocation mekanismLoc(String name) { return location(MEKANISM_MODID, name); }
  public static ResourceLocation adAstraLoc(String name) { return location(AD_ASTRA_MODID, name); }
  public static ResourceLocation arsElementalLoc(String name) { return location(ARS_ELEMENTAL_MODID, name); }
  public static ResourceLocation deeperDarkerLoc(String name) { return location(DEEPERDARKER_MODID, name); }
  public static ResourceLocation twilightLoc(String name) { return location(TWILIGHT_MODID, name); }
}
