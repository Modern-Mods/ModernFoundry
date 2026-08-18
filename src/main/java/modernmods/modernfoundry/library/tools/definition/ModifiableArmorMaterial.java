package modernmods.modernfoundry.library.tools.definition;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorType;
import modernmods.modernfoundry.library.tools.item.armor.DummyArmorMaterial;

import javax.annotation.Nullable;

/** Armor material that doubles as a container for tool definitions for each armor slot */
public class ModifiableArmorMaterial extends DummyArmorMaterial {
  /** Array of all four player armor item types. */
  public static final ArmorType[] ARMOR_TYPES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
  /** Array of all four armor slot types */
  public static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

  /** Array of slot index to tool definition for the slot */
  private final ToolDefinition[] armorDefinitions;

  private ModifiableArmorMaterial(Identifier id, SoundEvent equipSound, ToolDefinition... armorDefinitions) {
    super(id, equipSound);
    if (armorDefinitions.length != 4) {
      throw new IllegalArgumentException("Must have an armor definition for each slot");
    }
    this.armorDefinitions = armorDefinitions;
  }

  /** Creates a modifiable armor material, creates tool definition for the selected slots */
  public static ModifiableArmorMaterial create(Identifier id, SoundEvent equipSound, ArmorType... slots) {
    ToolDefinition[] definitions = new ToolDefinition[4];
    for (ArmorType slot : slots) {
      if (slot == ArmorType.BODY) {
        throw new IllegalArgumentException("Unsupported armor slot " + slot.getName());
      }
      definitions[slot.ordinal()] = ToolDefinition.create(id.withSuffix("_" + slot.getName()));
    }
    return new ModifiableArmorMaterial(id, equipSound, definitions);
  }

  /** Creates a modifiable armor material, creates tool definition for all four armor slots */
  public static ModifiableArmorMaterial create(Identifier id, SoundEvent equipSound) {
    return create(id, equipSound, ARMOR_TYPES);
  }

  /**
   * Gets the armor definition for the given armor slot, used in item construction
   * @param slotType  Slot type
   * @return  Armor definition
   */
  @Nullable
  public ToolDefinition getArmorDefinition(ArmorType slotType) {
    if (slotType == ArmorType.BODY) {
      return null;
    }
    return armorDefinitions[slotType.ordinal()];
  }
}
