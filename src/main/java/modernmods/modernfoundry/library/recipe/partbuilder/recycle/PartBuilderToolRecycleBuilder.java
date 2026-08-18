package modernmods.modernfoundry.library.recipe.partbuilder.recycle;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.mantle.recipe.ingredient.SizedIngredient;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.fluids.TinkerFluids;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.tables.recipe.PartBuilderToolRecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Builder for custom part builder tool recycling recipes.
 * Note we automatically add recycling for all tools in {@link TinkerTags.Items#MULTIPART_TOOL} unless they are also in {@link TinkerTags.Items#UNRECYCLABLE}; latter notably includes {@link TinkerTags.Items#ANCIENT_TOOLS}
 */
@RequiredArgsConstructor(staticName = "tools")
@Accessors(fluent = true)
public class PartBuilderToolRecycleBuilder extends AbstractRecipeBuilder<PartBuilderToolRecycleBuilder> {
  private final SizedIngredient tools;
  @Setter
  private Ingredient pattern = Ingredient.of(TinkerFluids.venomBottle);
  private final List<IMaterialItem> parts = new ArrayList<>();

  /** Creates a builder for the given tool */
  public static PartBuilderToolRecycleBuilder tool(ItemLike tool) {
    return tools(SizedIngredient.fromItems(tool));
  }

  /** Adds a part override to the builder. Order matches up to material indices */
  public PartBuilderToolRecycleBuilder part(IMaterialItem part) {
    parts.add(part);
    return this;
  }

  /** Adds a part override to the builder */
  public PartBuilderToolRecycleBuilder part(Supplier<? extends IMaterialItem> part) {
    return part(part.get());
  }

  @SuppressWarnings("deprecation")
  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(tools.getMatchingStacks().get(0).getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, Identifier id) {
    Identifier advancementId = buildOptionalAdvancement(id, "parts");
    consumer.accept(new LoadableFinishedRecipe<>(id, new PartBuilderToolRecycle(id, tools, pattern, parts), PartBuilderToolRecycle.LOADER, advancementId));
  }
}
