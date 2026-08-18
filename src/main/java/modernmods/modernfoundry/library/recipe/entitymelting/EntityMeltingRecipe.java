package modernmods.modernfoundry.library.recipe.entitymelting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.ICustomOutputRecipe;
import modernmods.mantle.recipe.container.IEmptyContainer;
import modernmods.mantle.recipe.helper.FluidOutput;
import modernmods.mantle.recipe.ingredient.EntityIngredient;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import java.util.Collection;

/**
 * Recipe to melt an entity into a fluid
 */
@RequiredArgsConstructor
public class EntityMeltingRecipe implements ICustomOutputRecipe<IEmptyContainer> {
  public static final RecordLoadable<EntityMeltingRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    EntityIngredient.LOADABLE.requiredField("entity", r -> r.ingredient),
    FluidOutput.Loadable.REQUIRED.requiredField("result", r -> r.output),
    IntLoadable.FROM_ONE.defaultField("damage", 2, true, r -> r.damage),
    EntityMeltingRecipe::new);

  @Getter
  private final Identifier id;
  @Getter
  private final EntityIngredient ingredient;
  private final FluidOutput output;
  @Getter
  private final int damage;

  /**
   * Checks if the recipe matches the given type
   * @param type  Type
   * @return  True if it matches
   */
  public boolean matches(EntityType<?> type) {
    return ingredient.test(type);
  }

  /** Gets the non-entity sensitive recipe result */
  public FluidStack getOutput() {
    return output.get();
  }

  /**
   * Gets the output for this recipe
   * @param entity  Entity being melted
   * @return  Fluid output
   */
  public FluidStack getOutput(LivingEntity entity) {
    return output.copy();
  }

  /**
   * Gets a collection of inputs for filtering in JEI
   * @return  Collection of types
   */
  public Collection<EntityType<?>> getInputs() {
    return ingredient.getTypes();
  }

  @Override
  public RecipeSerializer<? extends EntityMeltingRecipe> getSerializer() {
    return TinkerSmeltery.entityMeltingSerializer.get();
  }

  @Override
  public RecipeType<? extends EntityMeltingRecipe> getType() {
    return TinkerRecipeTypes.ENTITY_MELTING.get();
  }

  /** @deprecated use {@link #matches(EntityType)}*/
  @Deprecated
  @Override
  public boolean matches(IEmptyContainer inv, Level worldIn) {
    return false;
  }
}
