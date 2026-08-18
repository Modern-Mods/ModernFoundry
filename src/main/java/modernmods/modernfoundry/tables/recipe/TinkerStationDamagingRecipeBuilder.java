package modernmods.modernfoundry.tables.recipe;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;

import java.util.function.Consumer;

/** Builder for tinker station damaging recipes */
@RequiredArgsConstructor(staticName = "damage")
public class TinkerStationDamagingRecipeBuilder extends AbstractRecipeBuilder<TinkerStationDamagingRecipeBuilder> {

  private final Ingredient ingredient;
  private final int damageAmount;

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    ItemStack[] stacks = ingredient.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new);
    if (stacks.length == 0) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    save(consumer, BuiltInRegistries.ITEM.getKey(stacks[0].getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    if (ingredient.isEmpty()) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    Identifier advancementId = buildOptionalAdvancement(id, "tinker_station");
    consumer.accept(new LoadableFinishedRecipe<>(id, new TinkerStationDamagingRecipe(id, ingredient, damageAmount), TinkerStationDamagingRecipe.LOADER, advancementId));
  }
}
