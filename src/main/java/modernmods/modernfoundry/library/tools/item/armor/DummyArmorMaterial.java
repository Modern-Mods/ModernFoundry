package modernmods.modernfoundry.library.tools.item.armor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import modernmods.mantle.registration.object.IdAwareObject;

import java.util.Map;

/**
 * Armor material that returns 0 for all combat stats, since Tinkers armor derives its stats from tool stats rather than
 * the vanilla material. The equipment asset id is derived from the material id so the armor layer can be data-driven.
 */
@RequiredArgsConstructor
@Getter
public class DummyArmorMaterial implements IdAwareObject {
  private final Identifier id;
  private final SoundEvent equipSound;

  public String getName() {
    return id.toString();
  }

  /** Resource key for the equipment asset (armor layer definition) tied to this material */
  public ResourceKey<EquipmentAsset> getAssetId() {
    return ResourceKey.create(EquipmentAssets.ROOT_ID, id);
  }

  /** Builds the vanilla {@link ArmorMaterial} used to make the item equippable; all combat stats are zero as Tinkers supplies them from tool stats */
  public ArmorMaterial getArmorMaterial() {
    return new ArmorMaterial(0, Map.of(), 0, Holder.direct(equipSound), 0, 0, TagKey.create(Registries.ITEM, id.withPrefix("repair_material/")), getAssetId());
  }
}
