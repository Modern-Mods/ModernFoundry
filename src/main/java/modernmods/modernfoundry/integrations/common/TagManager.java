package modernmods.modernfoundry.integrations.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import modernmods.modernfoundry.integrations.data.integration.ModIntegration;

import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.location;
import static modernmods.modernfoundry.integrations.util.ResourceLocationHelper.resource;

public final class TagManager {

    public static final class Items {
        public static final TagKey<Item> BRONZE = forgeTag("storage_blocks/bronze");
        public static final TagKey<Item> BRONZE_INGOTS = forgeTag("ingots/bronze");
        public static final TagKey<Item> BRONZE_NUGGETS = forgeTag("nuggets/bronze");
        public static final TagKey<Item> CHEESE = forgeTag("food/cheese");

        // Botania
        public static final TagKey<Item> BOTANIA_LIVINGWOOD_LOGS = create("livingwood_logs");
        // Botania ModIntegration
        public static final TagKey<Item> INGOTS_TERRASTEEL = botaniaTag("terrasteel_ingots");
        public static final TagKey<Item> MYSTICAL_FLOWERS = botaniaTag("mystical_flowers");
        public static final TagKey<Item> DOUBLE_MYSTICAL_FLOWERS = botaniaTag("double_mystical_flowers");
        public static final TagKey<Item> LIVINGWOOD_LOGS = botaniaTag("livingwood_logs");
        public static final TagKey<Item> INGOTS_ELEMENTIUM = botaniaTag("elementium_ingots");
        public static final TagKey<Item> LIVINGWOOD_LOGS_GLIMMERING = botaniaTag("glimmering_livingwood_logs");

        // Malum
        public static final TagKey<Item> SOUL_STAINED_STEEL = forgeTag("storage_blocks/soul_stained_steel");
        public static final TagKey<Item> SOUL_STAINED_STEEL_INGOTS = forgeTag("ingots/soul_stained_steel");
        public static final TagKey<Item> SOUL_STAINED_STEEL_NUGGETS = forgeTag("nuggets/soul_stained_steel");

        // Ice and Fire: Dragons
        public static final TagKey<Item> WITHER_BONES = forgeTag("bones/wither");
        public static final TagKey<Item> DRAGONSTEEL_FIRE = forgeTag("storage_blocks/dragonsteel_fire");
        public static final TagKey<Item> DRAGONSTEEL_FIRE_INGOTS = forgeTag("ingots/dragonsteel_fire");
        public static final TagKey<Item> DRAGONSTEEL_ICE = forgeTag("storage_blocks/dragonsteel_ice");
        public static final TagKey<Item> DRAGONSTEEL_ICE_INGOTS = forgeTag("ingots/dragonsteel_ice");
        public static final TagKey<Item> DRAGONSTEEL_LIGHTNING = forgeTag("storage_blocks/dragonsteel_lightning");
        public static final TagKey<Item> DRAGONSTEEL_LIGHTNING_INGOTS = forgeTag("ingots/dragonsteel_lightning");

        // Ars Nouveau
        public static final TagKey<Item> SOURCE_GEM = forgeTag("gems/source_gem");
        public static final TagKey<Item> SOURCE_GEM_BLOCK = forgeTag("storage_blocks/source_gem");

        private static TagKey<Item> create(String id) { return TagKey.create(net.minecraft.core.registries.Registries.ITEM, resource(id)); }

        private static TagKey<Item> forgeTag(String name) { return TagKey.create(net.minecraft.core.registries.Registries.ITEM, forgeLoc(name)); }

        private static TagKey<Item> botaniaTag(String name) { return TagKey.create(net.minecraft.core.registries.Registries.ITEM, ModIntegration.botaniaLoc(name)); }
    }

    public static final class Blocks {
        public static final TagKey<Block> BRONZE = forgeTag("storage_blocks/bronze");

        // Malum
        public static final TagKey<Block> SOUL_STAINED_STEEL = forgeTag("storage_blocks/soul_stained_steel");

        // Ice and Fire: Dragons
        public static final TagKey<Block> DRAGONSTEEL_FIRE = forgeTag("storage_blocks/dragonsteel_fire");
        public static final TagKey<Block> DRAGONSTEEL_ICE = forgeTag("storage_blocks/dragonsteel_ice");
        public static final TagKey<Block> DRAGONSTEEL_LIGHTNING = forgeTag("storage_blocks/dragonsteel_lightning");

        // Ars Nouveau
        public static final TagKey<Block> SOURCE_GEM_BLOCK = forgeTag("storage_blocks/source_gem");

        private static TagKey<Block> create(String id) { return TagKey.create(net.minecraft.core.registries.Registries.BLOCK, resource(id)); }

        private static TagKey<Block> forgeTag(String name) { return TagKey.create(net.minecraft.core.registries.Registries.BLOCK, forgeLoc(name)); }
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> ELEMENTAL_SEVERING_MOBS = create("elemental_severing_mods");
        public static final TagKey<EntityType<?>> MILK_PRODUCER = create("milk_producer");

        private static TagKey<EntityType<?>> create(String id) { return TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, resource(id)); }
    }

    private static ResourceLocation forgeLoc(String path) {
        return location("forge", path);
    }

}
