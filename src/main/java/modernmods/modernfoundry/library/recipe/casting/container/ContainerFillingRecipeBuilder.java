package modernmods.modernfoundry.library.recipe.casting.container;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.recipe.data.AbstractRecipeBuilder;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.modernfoundry.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Builder for a container filling recipe. Takes an arbitrary fluid for a specific amount to fill a Forge {@link net.neoforged.neoforge.fluids.capability.IFluidHandlerItem}
 */
@AllArgsConstructor(staticName = "castingRecipe")
@SuppressWarnings({"WeakerAccess", "unused"})
public class ContainerFillingRecipeBuilder extends AbstractRecipeBuilder<ContainerFillingRecipeBuilder> {
  private final Identifier result;
  private final int fluidAmount;
  private final TypeAwareRecipeSerializer<? extends ContainerFillingRecipe> recipeSerializer;

  /**
   * Creates a new builder instance using the given result, amount, and serializer
   * @param result            Recipe result
   * @param fluidAmount       Container size
   * @param recipeSerializer  Serializer
   * @return  Builder instance
   */
  public static ContainerFillingRecipeBuilder castingRecipe(ItemLike result, int fluidAmount, TypeAwareRecipeSerializer<? extends ContainerFillingRecipe> recipeSerializer) {
    return new ContainerFillingRecipeBuilder(BuiltInRegistries.ITEM.getKey(result.asItem()), fluidAmount, recipeSerializer);
  }

  /**
   * Creates a new basin recipe builder using the given result, amount, and serializer
   * @param result            Recipe result
   * @param fluidAmount       Container size
   * @return  Builder instance
   */
  public static ContainerFillingRecipeBuilder basinRecipe(Identifier result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.basinFillingRecipeSerializer);
  }

  /**
   * Creates a new basin recipe builder using the given result, amount, and serializer
   * @param result            Recipe result
   * @param fluidAmount       Container size
   * @return  Builder instance
   */
  public static ContainerFillingRecipeBuilder basinRecipe(ItemLike result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.basinFillingRecipeSerializer);
  }

  /**
   * Creates a new table recipe builder using the given result, amount, and serializer
   * @param result            Recipe result
   * @param fluidAmount       Container size
   * @return  Builder instance
   */
  public static ContainerFillingRecipeBuilder tableRecipe(Identifier result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.tableFillingRecipeSerializer);
  }

  /**
   * Creates a new table recipe builder using the given result, amount, and serializer
   * @param result            Recipe result
   * @param fluidAmount       Container size
   * @return  Builder instance
   */
  public static ContainerFillingRecipeBuilder tableRecipe(ItemLike result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.tableFillingRecipeSerializer);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    this.save(consumer, this.result);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumerIn, Identifier id) {
    Identifier advancementId = this.buildOptionalAdvancement(id, "casting");
    consumerIn.accept(new ContainerFillingRecipeBuilder.Result(id, advancementId));
  }

  private class Result extends AbstractFinishedRecipe {
    public Result(Identifier ID, @Nullable Identifier advancementID) {
      super(ID, advancementID);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return recipeSerializer.serializer();
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (!group.isEmpty()) {
        json.addProperty("group", group);
      }
      json.addProperty("fluid_amount", fluidAmount);
      // TODO: consider another way to spoof this for datagen?
      json.addProperty("container", result.toString());
    }
  }
}
