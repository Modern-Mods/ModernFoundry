package modernmods.modernfoundry.common.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.data.CachedOutput;
import modernmods.mantle.recipe.data.FinishedRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import modernmods.mantle.data.GenericDataProvider;
import modernmods.mantle.recipe.data.IRecipeHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.utils.ResourceId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends GenericDataProvider implements IRecipeHelper {
  /**
   * 26.1 added a {@code HolderGetter<Item>} as the first argument to the vanilla recipe builders
   * (shaped/shapeless) and to {@code ItemPredicate.Builder.of}. Those resolve tag ingredients eagerly via
   * {@link net.minecraft.core.HolderGetter#getOrThrow(net.minecraft.tags.TagKey)}, but item tags are NOT bound
   * during datagen, so a plain registry lookup throws {@code "Missing tag ..."}. This wrapper returns an empty
   * {@link net.minecraft.core.HolderSet.Named} for any not-yet-bound tag, which serializes as a tag reference
   * (e.g. {@code {"tag":"c:ingots/gold"}}) without resolving its contents.
   */
  protected static final net.minecraft.core.HolderGetter<Item> ITEM_LOOKUP = new net.minecraft.core.HolderGetter<>() {
    private final net.minecraft.core.HolderGetter<Item> base = net.minecraft.core.registries.BuiltInRegistries.ITEM;

    @Override
    public java.util.Optional<net.minecraft.core.Holder.Reference<Item>> get(net.minecraft.resources.ResourceKey<Item> id) {
      return base.get(id);
    }

    @Override
    public java.util.Optional<net.minecraft.core.HolderSet.Named<Item>> get(net.minecraft.tags.TagKey<Item> tag) {
      return java.util.Optional.of(base.get(tag).orElseGet(() -> net.minecraft.core.HolderSet.emptyNamed(net.minecraft.core.registries.BuiltInRegistries.ITEM, tag)));
    }
  };

  public BaseRecipeProvider(PackOutput generator) {
    // "recipe" (singular): 1.21's datapack flattening renamed the recipes/ directory to recipe/. The game's
    // RecipeManager only reads data/<ns>/recipe/, so generating into the old plural directory would leave every recipe
    // unloaded.
    super(generator, Target.DATA_PACK, "recipe");
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  protected abstract void buildRecipes(Consumer<FinishedRecipe> consumer);

  @Override
  public abstract String getName();

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    List<CompletableFuture<?>> tasks = new ArrayList<>();
    buildRecipes(recipe -> tasks.add(saveJson(cache, recipe.getId(), recipe.serializeRecipe())));
    return allOf(tasks);
  }

  protected static Criterion<?> has(ItemLike item) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ITEM_LOOKUP, item).build());
  }

  protected static Criterion<?> has(TagKey<Item> tag) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ITEM_LOOKUP, tag).build());
  }

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }

  /* ResourceId overloads: 26.1 made Identifier final so our ID wrappers (ModifierId/MaterialId/MaterialStatsId)
     no longer ARE Identifiers. These delegate to the IRecipeHelper Identifier variants via getIdentifier(). */

  /** Gets a resource location for the mod prefixed onto the given ID's path */
  protected Identifier prefix(ResourceId location, String prefix) {
    return prefix(location.getIdentifier(), prefix);
  }

  /** Gets a resource location for the mod suffixed onto the given ID's path */
  protected Identifier suffix(ResourceId location, String suffix) {
    return suffix(location.getIdentifier(), suffix);
  }

  /** Gets a resource location for the mod wrapping the given ID's path */
  protected Identifier wrap(ResourceId location, String prefix, String suffix) {
    return wrap(location.getIdentifier(), prefix, suffix);
  }

  /* Recipe key helpers: 26.1 RecipeBuilder.save takes a ResourceKey<Recipe<?>>, and vanilla builders are bridged
     to Mantle's Consumer<FinishedRecipe> framework via VanillaFinishedRecipe.output(...). */

  /** Wraps a recipe id into the registry key vanilla builders now require */
  protected static net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeId(Identifier id) {
    return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id);
  }

  /** Wraps a recipe id string into the registry key vanilla builders now require */
  protected static net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeId(String id) {
    return recipeId(Identifier.parse(id));
  }
}
