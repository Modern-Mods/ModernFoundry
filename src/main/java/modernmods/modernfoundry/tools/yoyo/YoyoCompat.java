package modernmods.modernfoundry.tools.yoyo;

import java.util.ArrayList;
import java.util.List;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.common.registration.ItemDeferredRegisterExtension;
import modernmods.modernfoundry.tools.yoyo.compat.AdAstraYoyoItem;
import modernmods.modernfoundry.tools.yoyo.compat.ManaYoyoItem;
import modernmods.modernfoundry.tools.yoyo.compat.PigIronYoyoItem;
import modernmods.modernfoundry.tools.yoyo.compat.PressurizedYoyoItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.fml.ModList;

/** Registers the 1.21.1 reference's optional yoyo tiers without hard dependencies. */
public final class YoyoCompat {
  private static final List<RegisteredYoyo> YOYOS = new ArrayList<>();
  private static final List<ItemObject<Item>> CORDS = new ArrayList<>();

  private YoyoCompat() {}

  public static List<ItemObject<YoyoItem>> register(ItemDeferredRegisterExtension items) {
    registerFoundryTiers(items);
    registerIfLoaded(items, "ad_astra", YoyoCompat::adAstraTiers);
    registerIfLoaded(items, "botania", YoyoCompat::botaniaTiers);
    registerIfLoaded(items, "create", YoyoCompat::createTiers);
    registerIfLoaded(items, "draconicevolution", YoyoCompat::draconicTiers);
    registerIfLoaded(items, "forbidden_arcanus", YoyoCompat::forbiddenTiers);
    registerIfLoaded(items, "malum", YoyoCompat::malumTiers);
    registerIfLoaded(items, "mekanism", YoyoCompat::mekanismTiers);
    registerIfLoaded(items, "naturesaura", YoyoCompat::naturesAuraTiers);
    registerIfLoaded(items, "occultism", YoyoCompat::occultismTiers);
    registerIfLoaded(items, "pneumaticcraft", YoyoCompat::pneumaticTiers);
    registerIfLoaded(items, "redstone_arsenal", YoyoCompat::redstoneArsenalTiers);
    registerIfLoaded(items, "thermal", YoyoCompat::thermalTiers);
    registerIfLoaded(items, "twilightforest", YoyoCompat::twilightTiers);
    return YOYOS.stream().map(RegisteredYoyo::item).toList();
  }

  public static List<ItemObject<Item>> cords() {
    return CORDS;
  }

  private static void registerFoundryTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("modernfoundry", "amethyst_bronze", Tiers.IRON, 5.0, 8.0, 300, 5.5));
    register(items, spec("modernfoundry", "cobalt", Tiers.DIAMOND, 4.5, 10.0, 400, 6.0));
    register(items, spec("modernfoundry", "hepatizon", Tiers.DIAMOND, 5.0, 10.0, 500, 7.0));
    register(items, spec("modernfoundry", "manyullyn", Tiers.NETHERITE, 6.0, 11.0, 600, 7.5));
    register(items, spec("modernfoundry", "pigiron", Tiers.IRON, 4.5, 8.0, 250, 5.5).item("pig_iron_ingot").kind(Kind.PIG_IRON));
    register(items, spec("modernfoundry", "queens_slime", Tiers.DIAMOND, 6.0, 11.0, 500, 7.0));
    register(items, spec("modernfoundry", "rose_gold", Tiers.IRON, 5.5, 10.0, 500, 5.0));
    register(items, spec("modernfoundry", "slimesteel", Tiers.IRON, 5.5, 9.0, 350, 6.0));
  }

  private static void registerIfLoaded(ItemDeferredRegisterExtension items, String modId, TierSupplier supplier) {
    if (ModList.get().isLoaded(modId)) {
      supplier.register(items);
    }
  }

  private static void adAstraTiers(ItemDeferredRegisterExtension items) {
    cord(items, "cheese_cord");
    cord(items, "cheese_string");
    register(items, spec("ad_astra", "steel", Tiers.IRON, 5.5, 9.0, 500, 6.0));
    register(items, spec("ad_astra", "desh", Tiers.IRON, 4.5, 10.0, 400, 5.5).cord("cheese").kind(Kind.AD_ASTRA));
    register(items, spec("ad_astra", "ostrum", Tiers.DIAMOND, 5.0, 11.0, 500, 6.0).cord("cheese").kind(Kind.AD_ASTRA));
    register(items, spec("ad_astra", "calorite", Tiers.NETHERITE, 5.5, 12.0, 550, 6.5).cord("cheese").kind(Kind.AD_ASTRA));
  }

  private static void botaniaTiers(ItemDeferredRegisterExtension items) {
    cord(items, "mana_cord");
    register(items, spec("botania", "manasteel", Tiers.IRON, 4.5, 9.0, 400, 5.5).cord("mana").stick("botania:livingwood_twig").kind(Kind.MANA));
    register(items, spec("botania", "elementium", Tiers.DIAMOND, 5.0, 10.0, 500, 6.0).cord("mana").stick("botania:dreamwood_twig").kind(Kind.ELEMENTIUM));
    register(items, spec("botania", "terrasteel", Tiers.NETHERITE, 6.0, 11.0, 650, 7.0).cord("mana").stick("botania:livingwood_twig").kind(Kind.MANA_TERRASTEEL));
    register(items, spec("botania", "gaia_spirit", Tiers.NETHERITE, 5.0, 12.0, 700, 8.0).item("gaia_ingot").cord("mana").stick("botania:livingwood_twig").kind(Kind.GAIA));
    if (ModList.get().isLoaded("mythicbotany")) {
      register(items, spec("mythicbotany", "alfsteel", Tiers.NETHERITE, 6.0, 18.0, 800, 9.0).cord("mana").kind(Kind.MANA_TERRASTEEL).customRecipe("alfsteel_smithing"));
    }
  }

  private static void createTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("create", "andesite_alloy", Tiers.STONE, 4.5, 7.5, 250, 4.5));
    register(items, spec("create", "zinc", Tiers.IRON, 5.0, 8.0, 300, 5.0));
    register(items, spec("create", "brass", Tiers.DIAMOND, 5.0, 9.0, 400, 5.5));
  }

  private static void draconicTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("draconicevolution", "wyvern", Tiers.DIAMOND, 6.0, 8.0, 450, 6.0).customRecipe("wyvern_fusion"));
    register(items, spec("draconicevolution", "draconic", Tiers.NETHERITE, 7.0, 18.0, 800, 9.0).customRecipe("draconic_fusion"));
    register(items, spec("draconicevolution", "chaotic", Tiers.NETHERITE, 7.5, 18.0, 800, 10.0).customRecipe("chaotic_fusion"));
  }

  private static void forbiddenTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("forbidden_arcanus", "deorum", Tiers.DIAMOND, 6.0, 11.0, 550, 6.5));
    register(items, spec("forbidden_arcanus", "obsidian", Tiers.DIAMOND, 6.5, 6.0, 600, 6.0));
  }

  private static void malumTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("malum", "hallowed_gold", Tiers.IRON, 6.5, 12.0, 550, 5.0));
    register(items, spec("malum", "malignant_pewter", Tiers.NETHERITE, 6.0, 10.0, 600, 6.5));
    register(items, spec("malum", "soul_stained_steel", Tiers.DIAMOND, 5.5, 9.0, 450, 5.5));
  }

  private static void mekanismTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("mekanism", "tin", Tiers.IRON, 5.0, 8.0, 300, 5.0).item("ingot_tin"));
    register(items, spec("mekanism", "lead", Tiers.IRON, 6.0, 8.0, 300, 5.0).item("ingot_lead"));
    register(items, spec("mekanism", "osmium", Tiers.IRON, 4.5, 8.0, 300, 5.0).item("ingot_osmium"));
    register(items, spec("mekanism", "uranium", Tiers.IRON, 6.5, 7.0, 400, 5.5).item("ingot_uranium").kind(Kind.POISON));
    register(items, spec("mekanism", "steel", Tiers.DIAMOND, 5.5, 9.0, 500, 6.0).item("ingot_steel"));
    register(items, spec("mekanism", "bronze", Tiers.DIAMOND, 5.0, 9.0, 500, 6.0).item("ingot_bronze"));
    register(items, spec("mekanism", "refined_glowstone", Tiers.DIAMOND, 4.0, 9.5, 600, 6.5).item("ingot_refined_glowstone"));
    register(items, spec("mekanism", "refined_obsidian", Tiers.DIAMOND, 6.0, 9.5, 600, 6.5).item("ingot_refined_obsidian"));
  }

  private static void naturesAuraTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("naturesaura", "depth", Tiers.NETHERITE, 6.0, 11.0, 550, 6.5));
    register(items, spec("naturesaura", "sky", Tiers.DIAMOND, 4.0, 8.0, 350, 5.5));
  }

  private static void occultismTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("occultism", "silver", Tiers.IRON, 5.5, 8.0, 300, 5.0));
    register(items, spec("occultism", "iesnium", Tiers.DIAMOND, 5.1, 9.5, 550, 6.5));
  }

  private static void pneumaticTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("pneumaticcraft", "compressed_iron", Tiers.IRON, 5.5, 7.5, 350, 5.5).item("ingot_iron_compressed").kind(Kind.PRESSURIZED));
  }

  private static void redstoneArsenalTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("redstone_arsenal", "flux", Tiers.DIAMOND, 6.5, 14.0, 400, 6.5).stick("redstone_arsenal:flux_obsidian_rod"));
  }

  private static void thermalTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("thermal", "nickel", Tiers.IRON, 5.0, 8.0, 300, 5.0));
    register(items, spec("thermal", "tin", Tiers.IRON, 5.0, 8.0, 300, 5.0));
    register(items, spec("thermal", "lead", Tiers.IRON, 6.0, 8.0, 300, 5.0));
    register(items, spec("thermal", "silver", Tiers.IRON, 5.5, 8.0, 300, 5.0));
    register(items, spec("thermal", "bronze", Tiers.DIAMOND, 5.0, 9.0, 500, 6.0));
    register(items, spec("thermal", "constantan", Tiers.DIAMOND, 5.0, 9.0, 500, 6.0));
    register(items, spec("thermal", "invar", Tiers.DIAMOND, 5.0, 9.0, 500, 6.0));
    register(items, spec("thermal", "electrum", Tiers.DIAMOND, 6.0, 9.0, 500, 6.0));
    register(items, spec("thermal", "lumium", Tiers.NETHERITE, 3.5, 10.0, 550, 6.5).kind(Kind.GLOW));
    register(items, spec("thermal", "signalum", Tiers.NETHERITE, 4.0, 10.0, 550, 6.5));
    register(items, spec("thermal", "enderium", Tiers.NETHERITE, 4.5, 12.0, 600, 7.0).kind(Kind.ENDERIUM));
    if (ModList.get().isLoaded("thermal_integration")) {
      register(items, spec("thermal", "steel", Tiers.IRON, 5.5, 9.0, 500, 6.0));
      register(items, spec("thermal", "rose_gold", Tiers.IRON, 5.5, 10.0, 500, 5.0));
    }
  }

  private static void twilightTiers(ItemDeferredRegisterExtension items) {
    register(items, spec("twilightforest", "ironwood", Tiers.IRON, 4.0, 9.0, 350, 5.5));
    register(items, spec("twilightforest", "naga_scale", Tiers.IRON, 4.5, 9.0, 350, 5.5).item("naga_scale"));
    register(items, spec("twilightforest", "knightmetal", Tiers.DIAMOND, 5.0, 9.0, 450, 6.0));
    register(items, spec("twilightforest", "steeleaf", Tiers.DIAMOND, 4.0, 10.0, 450, 6.0));
    register(items, spec("twilightforest", "fiery", Tiers.NETHERITE, 6.0, 11.0, 600, 8.0));
  }

  private static void register(ItemDeferredRegisterExtension items, Spec spec) {
    if (YOYOS.stream().anyMatch(existing -> existing.spec.name.equals(spec.name))) {
      return;
    }
    YoyoTier tier = new YoyoTier(spec.name, spec.weight, spec.length, spec.duration, spec.damage, spec.tier)
      .addBlockInteraction(Interaction::breakBlocks, Interaction::craftWithBlock)
      .addEntityInteraction(Interaction::attackEntity, Interaction::collectItem);
    if (spec.kind == Kind.POISON) tier.addEntityInteraction(Interaction::poisonEntity);
    if (spec.kind == Kind.GLOW) tier.addEntityInteraction(Interaction::glowEntity);
    if (spec.kind == Kind.ENDERIUM) tier.addEntityInteraction(Interaction::enderiumEntity);
    ItemObject<YoyoItem> item = items.register(spec.name + "_yoyo", () -> create(spec, tier));
    YOYOS.add(new RegisteredYoyo(spec, item));
  }

  private static YoyoItem create(Spec spec, YoyoTier tier) {
    YoyoItem item = switch (spec.kind) {
      case AD_ASTRA -> new AdAstraYoyoItem(tier);
      case MANA -> new ManaYoyoItem(tier, ManaYoyoItem.Kind.MANA);
      case ELEMENTIUM -> new ManaYoyoItem(tier, ManaYoyoItem.Kind.ELEMENTIUM);
      case MANA_TERRASTEEL -> new ManaYoyoItem(tier, ManaYoyoItem.Kind.MANA_TERRASTEEL);
      case GAIA -> new ManaYoyoItem(tier, ManaYoyoItem.Kind.GAIA);
      case PIG_IRON -> new PigIronYoyoItem(tier);
      case PRESSURIZED -> new PressurizedYoyoItem(tier);
      default -> new YoyoItem(tier);
    };
    return item;
  }

  private static void cord(ItemDeferredRegisterExtension items, String name) {
    CORDS.add(items.register(name, () -> new Item(new Item.Properties())));
  }

  private static Spec spec(String modId, String name, Tier tier, double weight, double length, int duration, double damage) {
    return new Spec(modId, name, tier, weight, length, duration, damage, "", "", "minecraft:stick", Kind.BASE, "");
  }

  private enum Kind { BASE, AD_ASTRA, MANA, ELEMENTIUM, MANA_TERRASTEEL, GAIA, PIG_IRON, PRESSURIZED, POISON, GLOW, ENDERIUM }
  private interface TierSupplier { void register(ItemDeferredRegisterExtension items); }

  private static final class Spec {
    private final String modId, name, item, cord, stick, customRecipe;
    private final Tier tier;
    private final double weight, length, damage;
    private final int duration;
    private final Kind kind;

    private Spec(String modId, String name, Tier tier, double weight, double length, int duration, double damage, String item, String cord, String stick, Kind kind, String customRecipe) {
      this.modId = modId;
      this.name = name;
      this.tier = tier;
      this.weight = weight;
      this.length = length;
      this.duration = duration;
      this.damage = damage;
      this.item = item;
      this.cord = cord;
      this.stick = stick;
      this.kind = kind;
      this.customRecipe = customRecipe;
    }

    private Spec item(String value) { return copy(value, cord, stick, kind, customRecipe); }
    private Spec cord(String value) { return copy(item, value, stick, kind, customRecipe); }
    private Spec stick(String value) { return copy(item, cord, value, kind, customRecipe); }
    private Spec kind(Kind value) { return copy(item, cord, stick, value, customRecipe); }
    private Spec customRecipe(String value) { return copy(item, cord, stick, kind, value); }
    private Spec copy(String item, String cord, String stick, Kind kind, String customRecipe) {
      return new Spec(modId, name, tier, weight, length, duration, damage, item, cord, stick, kind, customRecipe);
    }
  }

  private record RegisteredYoyo(Spec spec, ItemObject<YoyoItem> item) {}
}
