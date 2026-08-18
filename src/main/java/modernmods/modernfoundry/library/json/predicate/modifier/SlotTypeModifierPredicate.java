package modernmods.modernfoundry.library.json.predicate.modifier;

import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.modifiers.ModifierRecipeLookup;
import modernmods.modernfoundry.library.tools.SlotType;

import javax.annotation.Nullable;

/** Predicate that matches any modifiers with recipes requiring a slot */
public record SlotTypeModifierPredicate(@Nullable SlotType slotType) implements ModifierPredicate {
  public static final RecordLoadable<SlotTypeModifierPredicate> LOADER = RecordLoadable.create(SlotType.LOADABLE.nullableField("slot", SlotTypeModifierPredicate::slotType), SlotTypeModifierPredicate::new);

  @Override
  public boolean matches(ModifierId input) {
    return ModifierRecipeLookup.isRecipeModifier(slotType, input);
  }

  @Override
  public RecordLoadable<SlotTypeModifierPredicate> getLoader() {
    return LOADER;
  }
}
