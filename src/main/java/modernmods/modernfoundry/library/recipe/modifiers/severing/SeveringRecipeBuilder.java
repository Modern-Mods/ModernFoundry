package modernmods.modernfoundry.library.recipe.modifiers.severing;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.ingredient.EntityIngredient;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Builder for entity melting recipes */
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "severing")
public class SeveringRecipeBuilder extends AbstractRecipeBuilder<SeveringRecipeBuilder> {
  private final EntityIngredient ingredient;
  private final ItemOutput output;
  private float baseChance = 0.05f;
  private float lootingBonus = 0.01f;
  @Nullable
  private ItemOutput childOutput = null;

  /** Creates a new builder from an item */
  public static SeveringRecipeBuilder severing(EntityIngredient ingredient, ItemLike output) {
    return SeveringRecipeBuilder.severing(ingredient, ItemOutput.fromItem(output));
  }

  /** Doubles the drop chances for this rare mob */
  public SeveringRecipeBuilder rareMob() {
    baseChance = 0.1f;
    lootingBonus = 0.02f;
    return this;
  }

  /**
   * Makes this an ageable severing recipe with no child output
   * @return  Builder instance
   */
  public SeveringRecipeBuilder noChildOutput() {
    return setChildOutput(ItemOutput.EMPTY);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(output.get().getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    Identifier advancementId = this.buildOptionalAdvancement(id, "severing");
    if (childOutput != null) {
      consumer.accept(new LoadableFinishedRecipe<>(id, new AgeableSeveringRecipe(id, ingredient, output, childOutput, baseChance, lootingBonus), AgeableSeveringRecipe.LOADER, advancementId));
    } else {
      consumer.accept(new LoadableFinishedRecipe<>(id, new SeveringRecipe(id, ingredient, output, baseChance, lootingBonus), SeveringRecipe.LOADER, advancementId));
    }
  }
}
