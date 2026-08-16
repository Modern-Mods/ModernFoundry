package modernmods.modernfoundry.library.recipe.tinkerstation.repairing;

import modernmods.hilt.data.loadable.field.LoadableField;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.modifiers.ModifierId;

/** Interface for serializing the modifier material repair recipes */
public interface IModifierMaterialRepairRecipe {
  /* Fields */
  LoadableField<ModifierId,IModifierMaterialRepairRecipe> MODIFIER_FIELD = ModifierId.PARSER.requiredField("modifier", IModifierMaterialRepairRecipe::getModifier);
  LoadableField<MaterialId,IModifierMaterialRepairRecipe> REPAIR_MATERIAL_FIELD = MaterialId.PARSER.requiredField("repair_material", IModifierMaterialRepairRecipe::getRepairMaterial);
  LoadableField<MaterialStatsId,IModifierMaterialRepairRecipe> STAT_TYPE_FIELD = MaterialStatsId.PARSER.requiredField("stat_type", IModifierMaterialRepairRecipe::getStatType);

  /** Gets the modifier required to apply this repair */
  ModifierId getModifier();

  /** Gets the material ID from the recipe */
  MaterialId getRepairMaterial();

  /** Gets the stat type used for repair, if null uses the first available stat type */
  MaterialStatsId getStatType();
}
