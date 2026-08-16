package modernmods.modernfoundry.world.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import modernmods.hilt.data.predicate.item.ItemPredicate;
import modernmods.hilt.recipe.data.ItemNameOutput;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.data.FakeRegistryEntry;
import modernmods.modernfoundry.library.data.tinkering.AbstractMobEquipmentProvider;
import modernmods.modernfoundry.library.materials.RandomMaterial;
import modernmods.modernfoundry.tools.TinkerTools;

import java.util.List;

/** Provider for custom mob equipment */
public class MobEquipmentProvider extends AbstractMobEquipmentProvider {
  public MobEquipmentProvider(PackOutput output) {
    super(output, TConstruct.MOD_ID);
  }

  @SuppressWarnings("removal")
  @Override
  protected void addEquipment() {
    RandomMaterial random = RandomMaterial.ancient();

    // piglins spawn with battle signs
    equip(TinkerTags.EntityTypes.PIGLINS)
      .slot(EquipmentSlot.MAINHAND)
      // only replace golden weapons, never a crossbow
      .match(ItemPredicate.set(Items.GOLDEN_SWORD, Items.GOLDEN_AXE))
      .tool(TinkerTools.battlesign)
      .material(random, random);
    // want different fluid lists for wither skeletons vs drowned
    equip(EntityType.DROWNED)
      .slot(EquipmentSlot.MAINHAND)
      // only replace empty hand
      .match(ItemPredicate.set(Items.AIR))
      .tool(TinkerTools.swasher)
      .fluid(TinkerTags.Fluids.DROWNED_SWASHER)
      .material(random, random, random);
    equip(EntityType.WITHER_SKELETON)
      .slot(EquipmentSlot.MAINHAND)
      .tool(TinkerTools.swasher)
      .fluid(TinkerTags.Fluids.WITHER_SKELETON_SWASHER)
      .material(random, random, random);
    // zombies spawn with melting pans
    equip("melting_pan", List.of(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK))
      .slot(EquipmentSlot.OFFHAND)
      .tool(TinkerTools.meltingPan)
      .material(random, random);
    // evil villagers spawn with war picks
    equip("war_pick", List.of(EntityType.ZOMBIE_VILLAGER, EntityType.VINDICATOR))
      .slot(EquipmentSlot.MAINHAND)
      .tool(TinkerTools.warPick)
      .material(random, random, random);
    // twilight forest compat
    String tf = "twilightforest";
    equip(FakeRegistryEntry.entity(ResourceLocation.fromNamespaceAndPath(tf, "minotaur")), new ModLoadedCondition(tf))
      .slot(EquipmentSlot.MAINHAND)
      .tool(ItemNameOutput.fromName(TinkerTools.minotaurAxe.getId()))
      .material(random, random, random);
  }

  @Override
  public String getName() {
    return "Modern Foundry mob equipment";
  }
}
