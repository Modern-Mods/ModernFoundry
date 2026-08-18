package modernmods.modernfoundry.library.recipe.tinkerstation.repairing;

import net.minecraft.world.item.crafting.Ingredient;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.modernfoundry.library.materials.definition.MaterialId;

/**
 * Interface for serializing the recipe
 */
public interface ISpecializedRepairRecipe {
  /* Fields */
  LoadableField<Ingredient,ISpecializedRepairRecipe> TOOL_FIELD = IngredientLoadable.DISALLOW_EMPTY.requiredField("tool", ISpecializedRepairRecipe::getTool);
  LoadableField<MaterialId,ISpecializedRepairRecipe> REPAIR_MATERIAL_FIELD = MaterialId.PARSER.requiredField("repair_material", ISpecializedRepairRecipe::getRepairMaterial);

  /** Gets the tool ingredient from the recipe */
  Ingredient getTool();
  /** Gets the material ID from the recipe */
  MaterialId getRepairMaterial();
}
