package modernmods.modernfoundry.tools;

import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.library.tools.definition.ModifiableArmorMaterial;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;

import static modernmods.modernfoundry.TConstruct.getResource;

public class ArmorDefinitions {
   /** Balanced armor set */
  public static final ModifiableArmorMaterial TRAVELERS = ModifiableArmorMaterial.create(getResource("travelers"), Sounds.EQUIP_TRAVELERS.getSound());
  public static final ToolDefinition TRAVELERS_SHIELD = ToolDefinition.create(TinkerTools.travelersShield);

  /** High defense armor set */
  public static final ModifiableArmorMaterial PLATE = ModifiableArmorMaterial.create(getResource("plate"), Sounds.EQUIP_PLATE.getSound());
  public static final ToolDefinition PLATE_SHIELD = ToolDefinition.create(TinkerTools.plateShield);

  /** High modifiers armor set */
  public static final ModifiableArmorMaterial SLIMESUIT = ModifiableArmorMaterial.create(getResource("slime"), Sounds.EQUIP_SLIME.getSound());
  public static final ToolDefinition SLIME_WINGS = ToolDefinition.create(TinkerTools.slimeWings);
}
