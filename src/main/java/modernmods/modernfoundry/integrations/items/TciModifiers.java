package modernmods.modernfoundry.integrations.items;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.integrations.common.TciModule;
import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.items.modifiers.armor.AethermancerModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.AlfheimModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.AquamancerModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.ArsNouveauModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.BisonFurModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.CrocodileModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.EnchantersShieldModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.FrontierCapModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.GeomancerModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.GreatFairyModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.MasticateModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.MosquitoModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.PoseidonModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.PyromancerModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.RoadrunnerModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.SculkingModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.ShieldOfTheDeepModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.TerrestrialModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.TurtleShellModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.AlfModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.CapturingModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.CheesyModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.ElementalModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.FlamedModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.ForgottenModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.FroststeelModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.GlowUpModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.IcedModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.MechanicalArmModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.PhantasmalModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.PrecipitateModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.SirenModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.TerraModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.TwilitModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.UtheriumModifier;
import modernmods.modernfoundry.integrations.items.modifiers.tool.ZappedModifier;
import modernmods.modernfoundry.integrations.items.modifiers.traits.DragonScalesModifier;
import modernmods.modernfoundry.integrations.items.modifiers.traits.KineticModifier;
import modernmods.modernfoundry.integrations.items.modifiers.traits.ManaModifier;
import modernmods.modernfoundry.integrations.items.modifiers.traits.OxygenatedModifier;
import modernmods.modernfoundry.integrations.items.modifiers.traits.SoulStained;
import modernmods.modernfoundry.integrations.items.modifiers.traits.WaterPowered;
import modernmods.modernfoundry.integrations.items.modifiers.armor.EngineersGogglesModifier;
import modernmods.modernfoundry.integrations.items.modifiers.armor.MultiVisionModifier;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.integrations.items.modifiers.tool.ModerateModifier;
import modernmods.modernfoundry.library.modifiers.util.ModifierDeferredRegister;
import modernmods.modernfoundry.library.modifiers.util.StaticModifier;

/** Native modifier register for TciIntegration content. */
public final class TciModifiers extends TciModule {
  private static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TConstruct.MOD_ID);

  public static StaticModifier<ManaModifier> MANA_MODIFIER;
  public static StaticModifier<TerraModifier> TERRA_MODIFIER;
  public static StaticModifier<ElementalModifier> ELEMENTAL_MODIFIER;
  public static StaticModifier<TerrestrialModifier> TERRESTRIAL_MODIFIER;
  public static StaticModifier<GreatFairyModifier> GREAT_FAIRY_MODIFIER;
  public static StaticModifier<ModerateModifier> MODERATE_MODIFIER;
  public static StaticModifier<MechanicalArmModifier> MECHANICAL_ARM_MODIFIER;
  public static StaticModifier<WaterPowered> WATER_POWERED_MODIFIER;
  public static StaticModifier<PoseidonModifier> POSEIDON_MODIFIER;
  public static StaticModifier<SirenModifier> SIREN_MODIFIER;
  public static StaticModifier<ArsNouveauModifier> ARS_MODIFIER;
  public static StaticModifier<EnchantersShieldModifier> ENCHANTERS_SHIELD_MODIFIER;
  public static StaticModifier<FrontierCapModifier> FRONTIER_CAP_MODIFIER;
  public static StaticModifier<RoadrunnerModifier> ROADRUNNER_MODIFIER;
  public static StaticModifier<TurtleShellModifier> TURTLE_SHELL_MODIFIER;
  public static StaticModifier<BisonFurModifier> BISON_FUR_MODIFIER;
  public static StaticModifier<ShieldOfTheDeepModifier> SHIELD_OF_THE_DEEP_MODIFIER;
  public static StaticModifier<MosquitoModifier> MOSQUITO_MODIFIER;
  public static StaticModifier<CrocodileModifier> CROCODILE_MODIFIER;
  public static StaticModifier<SoulStained> SOUL_STAINED_MODIFIER;
  public static StaticModifier<MasticateModifier> MASTICATE_MODIFIER;
  public static StaticModifier<UtheriumModifier> UTHERIUM_MODIFIER;
  public static StaticModifier<FroststeelModifier> FROSTSTEEL_MODIFIER;
  public static StaticModifier<ForgottenModifier> FORGOTTEN_MODIFIER;
  public static StaticModifier<CheesyModifier> CHEESY_MODIFIER;
  public static StaticModifier<OxygenatedModifier> OXYGENATED_MODIFIER;
  public static StaticModifier<AlfheimModifier> ALFHEIM_MODIFIER;
  public static StaticModifier<AlfModifier> ALF_MODIFIER;
  public static StaticModifier<KineticModifier> KINETIC_MODIFIER;
  public static StaticModifier<GlowUpModifier> GLOWUP_MODIFIER;
  public static StaticModifier<FlamedModifier> FLAMED_MODIFIER;
  public static StaticModifier<IcedModifier> ICED_MODIFIER;
  public static StaticModifier<ZappedModifier> ZAPPED_MODIFIER;
  public static StaticModifier<PhantasmalModifier> PHANTASMAL_MODIFIER;
  public static StaticModifier<DragonScalesModifier> DRAGON_SCALES_MODIFIER;
  public static StaticModifier<CapturingModifier> CAPTURING_MODIFIER;
  public static StaticModifier<AethermancerModifier> AETHERMANCER_MODIFIER;
  public static StaticModifier<AquamancerModifier> AQUAMANCER_MODIFIER;
  public static StaticModifier<GeomancerModifier> GEOMANCER_MODIFIER;
  public static StaticModifier<PyromancerModifier> PYROMANCER_MODIFIER;
  public static StaticModifier<SculkingModifier> SCULKING_MODIFIER;
  public static StaticModifier<PrecipitateModifier> PRECIPITATE_MODIFIER;
  public static StaticModifier<TwilitModifier> TWILIT_MODIFIER;

  public static void init() {
    if (ModIntegration.canLoad(ModIntegration.BOTANIA_MODID)) {
      MANA_MODIFIER = MODIFIERS.register("mana", ManaModifier::new);
      TERRA_MODIFIER = MODIFIERS.register("terra", TerraModifier::new);
      ELEMENTAL_MODIFIER = MODIFIERS.register("elemental", ElementalModifier::new);
      TERRESTRIAL_MODIFIER = MODIFIERS.register("terrestrial", TerrestrialModifier::new);
      GREAT_FAIRY_MODIFIER = MODIFIERS.register("great_fairy", GreatFairyModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.CREATE_MODID)) {
      MECHANICAL_ARM_MODIFIER = MODIFIERS.register("mechanical_arm", MechanicalArmModifier::new);
      ModifierModule.LOADER.register(TConstruct.getResource("engineers_goggles"), EngineersGogglesModifier.INSTANCE.getLoader());
    }
    if (ModIntegration.canLoad(ModIntegration.AQUACULTURE_MODID)) {
      WATER_POWERED_MODIFIER = MODIFIERS.register("water_powered", WaterPowered::new);
      POSEIDON_MODIFIER = MODIFIERS.register("poseidon", PoseidonModifier::new);
      SIREN_MODIFIER = MODIFIERS.register("siren", SirenModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.ARS_MODID)) {
      ARS_MODIFIER = MODIFIERS.register("ars_nouveau", ArsNouveauModifier::new);
      ENCHANTERS_SHIELD_MODIFIER = MODIFIERS.register("enchanters_shield", EnchantersShieldModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.ALEX_MODID)) {
      ROADRUNNER_MODIFIER = MODIFIERS.register("roadrunner", RoadrunnerModifier::new);
      FRONTIER_CAP_MODIFIER = MODIFIERS.register("frontier_cap", FrontierCapModifier::new);
      TURTLE_SHELL_MODIFIER = MODIFIERS.register("turtle_shell", TurtleShellModifier::new);
      BISON_FUR_MODIFIER = MODIFIERS.register("bison_fur", BisonFurModifier::new);
      SHIELD_OF_THE_DEEP_MODIFIER = MODIFIERS.register("shield_of_the_deep", ShieldOfTheDeepModifier::new);
      MOSQUITO_MODIFIER = MODIFIERS.register("mosquito", MosquitoModifier::new);
      CROCODILE_MODIFIER = MODIFIERS.register("crocodile", CrocodileModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.MALUM_MODID)) SOUL_STAINED_MODIFIER = MODIFIERS.register("soul_stained", SoulStained::new);
    if (ModIntegration.canLoad(ModIntegration.UNDERGARDEN_MODID)) {
      MASTICATE_MODIFIER = MODIFIERS.register("masticate", MasticateModifier::new);
      UTHERIUM_MODIFIER = MODIFIERS.register("utherium", UtheriumModifier::new);
      FROSTSTEEL_MODIFIER = MODIFIERS.register("froststeel", FroststeelModifier::new);
      FORGOTTEN_MODIFIER = MODIFIERS.register("forgotten", ForgottenModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.IE_MODID)) ModifierModule.LOADER.register(TConstruct.getResource("multivision"), MultiVisionModifier.INSTANCE.getLoader());
    if (ModIntegration.canLoad(ModIntegration.MEKANISM_MODID)) {
      KINETIC_MODIFIER = MODIFIERS.register("kinetic", KineticModifier::new);
      GLOWUP_MODIFIER = MODIFIERS.register("glowup", GlowUpModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.MYTHIC_BOTANY_MODID)) {
      ALFHEIM_MODIFIER = MODIFIERS.register("alfheim", AlfheimModifier::new);
      ALF_MODIFIER = MODIFIERS.register("alf", AlfModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.IFD_MODID)) {
      FLAMED_MODIFIER = MODIFIERS.register("flamed", FlamedModifier::new);
      ICED_MODIFIER = MODIFIERS.register("iced", IcedModifier::new);
      ZAPPED_MODIFIER = MODIFIERS.register("zapped", ZappedModifier::new);
      PHANTASMAL_MODIFIER = MODIFIERS.register("phantasmal", PhantasmalModifier::new);
      DRAGON_SCALES_MODIFIER = MODIFIERS.register("dragonscales", DragonScalesModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.AD_ASTRA_MODID) || ModIntegration.canLoad(ModIntegration.BEYOND_EARTH_MODID)) {
      CHEESY_MODIFIER = MODIFIERS.register("cheesy", CheesyModifier::new);
      OXYGENATED_MODIFIER = MODIFIERS.register("oxygenated", OxygenatedModifier::new);
    }
    MODERATE_MODIFIER = MODIFIERS.register("moderate", ModerateModifier::new);
    if (ModIntegration.canLoad(ModIntegration.APOTH_MODID)) CAPTURING_MODIFIER = MODIFIERS.register("capturing", CapturingModifier::new);
    if (ModIntegration.canLoad(ModIntegration.ARS_ELEMENTAL_MODID)) {
      AETHERMANCER_MODIFIER = MODIFIERS.register("aethermancer", AethermancerModifier::new);
      AQUAMANCER_MODIFIER = MODIFIERS.register("aquamancer", AquamancerModifier::new);
      GEOMANCER_MODIFIER = MODIFIERS.register("geomancer", GeomancerModifier::new);
      PYROMANCER_MODIFIER = MODIFIERS.register("pyromancer", PyromancerModifier::new);
    }
    if (ModIntegration.canLoad(ModIntegration.DEEPERDARKER_MODID)) SCULKING_MODIFIER = MODIFIERS.register("sculking", SculkingModifier::new);
    if (ModIntegration.canLoad(ModIntegration.TWILIGHT_MODID)) {
      PRECIPITATE_MODIFIER = MODIFIERS.register("precipitate", PrecipitateModifier::new);
      TWILIT_MODIFIER = MODIFIERS.register("twilit", TwilitModifier::new);
    }
  }

  public TciModifiers() {
    MODIFIERS.register(TConstruct.getModBus());
  }
}
