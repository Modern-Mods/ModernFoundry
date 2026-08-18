package modernmods.modernfoundry.tools.recipe;

import com.mojang.datafixers.util.Function6;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.ingredient.SizedIngredient;
import modernmods.modernfoundry.library.json.predicate.modifier.ModifierPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.worktable.AbstractSizedIngredientRecipeBuilder;
import modernmods.modernfoundry.library.tools.SlotType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Builder for {@link ModifierRemovalRecipe} and {@link ExtractModifierRecipe} */
@RequiredArgsConstructor(staticName = "removal")
public class ModifierRemovalRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ModifierRemovalRecipeBuilder> {
  private final Function6<Identifier,String,SizedIngredient,List<SizedIngredient>,List<ItemOutput>,IJsonPredicate<ModifierId>,ModifierRemovalRecipe> constructor;
  private final List<ItemOutput> leftovers = new ArrayList<>();
  @Accessors(chain = true)
  @Setter
  private String name = "modifiers";
  private SizedIngredient tools = ModifierRemovalRecipe.DEFAULT_TOOLS;
  @Accessors(fluent = true)
  @Setter
  private IJsonPredicate<ModifierId> modifierPredicate = ModifierPredicate.ANY;

  public static ModifierRemovalRecipeBuilder removal() {
    return removal(ModifierRemovalRecipe::new);
  }

  public static ModifierRemovalRecipeBuilder extract() {
    return removal(ExtractModifierRecipe::new);
  }

  /** Sets the name from the given slot */
  public ModifierRemovalRecipeBuilder slotName(SlotType slot) {
    return setName(slot.getName());
  }

  /**
   * Sets the tool requirement for this recipe
   */
  public ModifierRemovalRecipeBuilder setTools(SizedIngredient ingredient) {
    this.tools = ingredient;
    return this;
  }

  /**
   * Sets the tool requirement for this recipe
   */
  public ModifierRemovalRecipeBuilder setTools(Ingredient ingredient) {
    return setTools(SizedIngredient.of(ingredient));
  }

  /**
   * Adds a leftover stack to the recipe
   */
  public ModifierRemovalRecipeBuilder addLeftover(ItemStack stack) {
    leftovers.add(ItemOutput.fromStack(stack));
    return this;
  }

  /**
   * Adds a leftover stack to the recipe
   */
  public ModifierRemovalRecipeBuilder addLeftover(ItemLike item) {
    // ItemOutput.fromItem defers stack construction: an ItemStack cannot be built until components are bound (not so at datagen)
    leftovers.add(ItemOutput.fromItem(item));
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(leftovers.get(0).getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one input");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new LoadableFinishedRecipe<>(id, constructor.apply(id, name, tools, inputs, leftovers, modifierPredicate), ModifierRemovalRecipe.LOADER, advancementId));
  }
}
