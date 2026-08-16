package modernmods.modernfoundry.library.recipe.modifiers.adding;

import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.tools.SlotType;

import javax.annotation.Nullable;

/** Interface for all recipes that add a modifier, for looking up recipe usage */
public interface IModifierRecipe {
  /** Gets the modifier that this recipe adds */
  Modifier getModifier();

  /** Gets the type of slot used by this recipe */
  @Nullable
  default SlotType getSlotType() {
    return null;
  }
}
