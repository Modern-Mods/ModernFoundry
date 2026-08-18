package modernmods.modernfoundry.library.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.mantle.registration.deferred.SynchronizedDeferredRegister;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.recipe.alloying.AlloyRecipe;
import modernmods.modernfoundry.library.recipe.casting.ICastingRecipe;
import modernmods.modernfoundry.library.recipe.entitymelting.EntityMeltingRecipe;
import modernmods.modernfoundry.library.recipe.fuel.MeltingFuel;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipe;
import modernmods.modernfoundry.library.recipe.melting.IMeltingRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.library.recipe.molding.MoldingRecipe;
import modernmods.modernfoundry.library.recipe.partbuilder.IPartBuilderRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.library.recipe.worktable.IModifierWorktableRecipe;

/**
 * Class containing all of Tinkers Construct recipe types
 */
public class TinkerRecipeTypes {
  /** Deferred instance */
  private static final SynchronizedDeferredRegister<RecipeType<?>> TYPES = SynchronizedDeferredRegister.create(Registries.RECIPE_TYPE, TConstruct.MOD_ID);

  public static final DeferredHolder<? super RecipeType<IPartBuilderRecipe>, RecipeType<IPartBuilderRecipe>> PART_BUILDER = register("part_builder");
  public static final DeferredHolder<? super RecipeType<MaterialRecipe>, RecipeType<MaterialRecipe>> MATERIAL = register("material");
  public static final DeferredHolder<? super RecipeType<ITinkerStationRecipe>, RecipeType<ITinkerStationRecipe>> TINKER_STATION = register("tinker_station");
  public static final DeferredHolder<? super RecipeType<IModifierWorktableRecipe>, RecipeType<IModifierWorktableRecipe>> MODIFIER_WORKTABLE = register("modifier_worktable");

  // casting
  public static final DeferredHolder<? super RecipeType<ICastingRecipe>, RecipeType<ICastingRecipe>> CASTING_BASIN = register("casting_basin");
  public static final DeferredHolder<? super RecipeType<ICastingRecipe>, RecipeType<ICastingRecipe>> CASTING_TABLE = register("casting_table");
  public static final DeferredHolder<? super RecipeType<MoldingRecipe>, RecipeType<MoldingRecipe>> MOLDING_TABLE = register("molding_table");
  public static final DeferredHolder<? super RecipeType<MoldingRecipe>, RecipeType<MoldingRecipe>> MOLDING_BASIN = register("molding_basin");

  // smeltery
  public static final DeferredHolder<? super RecipeType<IMeltingRecipe>, RecipeType<IMeltingRecipe>> MELTING = register("melting");
  public static final DeferredHolder<? super RecipeType<EntityMeltingRecipe>, RecipeType<EntityMeltingRecipe>> ENTITY_MELTING = register("entity_melting");
  public static final DeferredHolder<? super RecipeType<MeltingFuel>, RecipeType<MeltingFuel>> FUEL = register("fuel");
  public static final DeferredHolder<? super RecipeType<AlloyRecipe>, RecipeType<AlloyRecipe>> ALLOYING = register("alloying");

  // modifiers
  public static final DeferredHolder<? super RecipeType<SeveringRecipe>, RecipeType<SeveringRecipe>> SEVERING = register("severing");

  /** Internal recipe type for recipes that are not pulled by any specific crafting block */
  public static final DeferredHolder<? super RecipeType<Recipe<?>>, RecipeType<Recipe<?>>> DATA = register("data");

  /** Initializes the deferred register */
  public static void init(IEventBus bus) {
    TYPES.register(bus);
  }

  /**
   * Registers a new recipe type, prefixing with the mod ID
   * @param name  Recipe type name
   * @param <T>   Recipe type
   * @return  Registered recipe type
   */
  static <T extends Recipe<?>> DeferredHolder<? super RecipeType<T>, RecipeType<T>> register(String name) {
    return TYPES.register(name, () -> new RecipeType<>() {
      @Override
      public String toString() {
        return TConstruct.MOD_ID + ":" + name;
      }
    });
  }
}
