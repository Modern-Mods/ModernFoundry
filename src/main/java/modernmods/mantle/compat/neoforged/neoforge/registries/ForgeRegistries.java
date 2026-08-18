package modernmods.mantle.compat.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Adapter mirroring the old Forge {@code ForgeRegistries} constants on top of NeoForge's
 * {@link BuiltInRegistries}. Only the registries referenced by the ported code are exposed.
 */
public final class ForgeRegistries {
  private ForgeRegistries() {}

  public static final IForgeRegistry<Block> BLOCKS = new ForgeRegistry<>(BuiltInRegistries.BLOCK);
  public static final IForgeRegistry<Item> ITEMS = new ForgeRegistry<>(BuiltInRegistries.ITEM);
  public static final IForgeRegistry<Fluid> FLUIDS = new ForgeRegistry<>(BuiltInRegistries.FLUID);
  public static final IForgeRegistry<MobEffect> MOB_EFFECTS = new ForgeRegistry<>(BuiltInRegistries.MOB_EFFECT);
  public static final IForgeRegistry<EntityType<?>> ENTITY_TYPES = new ForgeRegistry<>(BuiltInRegistries.ENTITY_TYPE);
  public static final IForgeRegistry<Potion> POTIONS = new ForgeRegistry<>(BuiltInRegistries.POTION);
  public static final IForgeRegistry<SoundEvent> SOUND_EVENTS = new ForgeRegistry<>(BuiltInRegistries.SOUND_EVENT);

  /** Registry keys, mirroring the old {@code ForgeRegistries.Keys} */
  public static final class Keys {
    private Keys() {}

    public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = NeoForgeRegistries.Keys.BIOME_MODIFIERS;
  }
}
