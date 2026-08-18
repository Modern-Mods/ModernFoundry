package modernmods.modernfoundry.tools.recipe;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.json.predicate.modifier.ModifierPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.worktable.AbstractSizedIngredientRecipeBuilder;

import java.util.function.Consumer;

/** Builder for an enchantment converting recipe */
@RequiredArgsConstructor(staticName = "converting")
public class EnchantmentConvertingRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<EnchantmentConvertingRecipeBuilder> {
  private final String name;
  private final boolean matchBook;
  private boolean returnInput = false;
  @Setter
  @Accessors(fluent = true)
  private IJsonPredicate<ModifierId> modifierPredicate = ModifierPredicate.ANY;

  /**
   * If true, returns the unenchanted form of the item as an extra result
   */
  public EnchantmentConvertingRecipeBuilder returnInput() {
    returnInput = true;
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, TConstruct.getResource(name));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one input");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, new EnchantmentConvertingRecipe(id, name, inputs, matchBook, returnInput, modifierPredicate), EnchantmentConvertingRecipe.LOADER, advancementId));
  }
}
