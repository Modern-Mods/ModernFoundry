package modernmods.modernfoundry.library.recipe.tinkerstation.repairing;

import lombok.RequiredArgsConstructor;
import modernmods.hilt.recipe.data.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import modernmods.hilt.recipe.data.AbstractRecipeBuilder;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.util.LazyModifier;

import java.util.function.Consumer;

/** @deprecated use {@link modernmods.modernfoundry.library.modifiers.modules.behavior.MaterialRepairModule} */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor(staticName = "repair")
public class ModifierMaterialRepairRecipeBuilder extends AbstractRecipeBuilder<ModifierMaterialRepairRecipeBuilder> {
  private final ModifierId modifier;
  private final MaterialId material;
  private final MaterialStatsId statType;

  public static ModifierMaterialRepairRecipeBuilder repair(LazyModifier modifier, MaterialId material, MaterialStatsId statType) {
    return repair(modifier.getId(), material, statType);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, modifier);
  }

  /** Builds the recipe for the crafting table using a repair kit */
  @SuppressWarnings("removal")
  public ModifierMaterialRepairRecipeBuilder saveCraftingTable(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "tinker_station");
    consumer.accept(new LoadableFinishedRecipe<>(id, new ModifierMaterialRepairKitRecipe(id, modifier, material, statType), ModifierMaterialRepairKitRecipe.LOADER, advancementId));
    return this;
  }

  @SuppressWarnings("removal")
  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "tinker_station");
    consumer.accept(new LoadableFinishedRecipe<>(id, new ModifierMaterialRepairRecipe(id, modifier, material, statType), ModifierMaterialRepairRecipe.LOADER, advancementId));
  }
}
