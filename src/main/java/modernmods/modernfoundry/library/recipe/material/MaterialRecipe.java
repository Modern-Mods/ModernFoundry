package modernmods.modernfoundry.library.recipe.material;

import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.ICustomOutputRecipe;
import modernmods.mantle.recipe.container.ISingleStackContainer;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariant;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;
import modernmods.modernfoundry.tables.TinkerTables;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recipe to get the material from an ingredient
 */
public class MaterialRecipe implements ICustomOutputRecipe<ISingleStackContainer>, IMaterialValue {
  /** Empty material instance for the cache */
  @SuppressWarnings("removal")
  public static final MaterialRecipe EMPTY = new MaterialRecipe(Identifier.parse("missingno"), "", null, 0, 0, IMaterial.UNKNOWN_ID, ItemOutput.EMPTY);
  public static final RecordLoadable<MaterialRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", MaterialRecipe::getIngredient),
    IMaterialValue.VALUE_FIELD,
    IMaterialValue.NEEDED_FIELD,
    MaterialVariantId.LOADABLE.requiredField("material", r -> r.getMaterial().getVariant()),
    ItemOutput.Loadable.OPTIONAL_STACK.emptyField("leftover", r -> r.leftover),
    MaterialRecipe::new);

  /** Vanilla requires 4 ingots for full repair, we drop it down to 3 to mesh better with nuggets and blocks and to fit small head costs better */
  public static final float INGOTS_PER_REPAIR = 3f;

  @Getter
  protected final Identifier id;
  @Getter
  protected final String group;
  @Getter
  protected final Ingredient ingredient;
  /** Amount of material this recipe returns */
  @Getter
  protected final int value;
  /** Amount of input items needed to craft this material */
  @Getter
  protected final int needed;
  /** Material ID for the recipe return */
  @Getter
  protected final MaterialVariant material;
  /** Leftover stack of value 1, used if the value is more than 1 */
  protected final ItemOutput leftover;

  /**
   * Creates a new material recipe
   */
  @SuppressWarnings("WeakerAccess")
  public MaterialRecipe(Identifier id, String group, Ingredient ingredient, int value, int needed, MaterialVariantId materialId, ItemOutput leftover) {
    this.id = id;
    this.group = group;
    this.ingredient = ingredient;
    this.value = value;
    this.needed = needed;
    this.material = MaterialVariant.of(materialId);
    // ignore leftover if the value is 1, its useless to us
    this.leftover = value > 1 ? leftover : ItemOutput.EMPTY;

    // save recipe into the cache
    MaterialRecipeCache.registerRecipe(this);
  }

  /* Basic */

  @Override
  public RecipeType<? extends MaterialRecipe> getType() {
    return TinkerRecipeTypes.MATERIAL.get();
  }

  public ItemStack getToastSymbol() {
    return new ItemStack(TinkerTables.partBuilder);
  }

  @Override
  public RecipeSerializer<? extends MaterialRecipe> getSerializer() {
    return TinkerTables.materialRecipeSerializer.get();
  }

  @Override
  public boolean hasLeftover() {
    return !this.leftover.isEmpty();
  }

  @Override
  public ItemStack getLeftover() {
    return this.leftover.get().copy();
  }

  /* Material methods */

  @Override
  public boolean matches(ISingleStackContainer inv, Level worldIn) {
    return !material.isUnknown() && this.ingredient.test(inv.getStack());
  }

  /**
   * Finds the material recipe matching the given inventory, reading from the correct recipe source per side. 26.1 removed
   * {@code Level#getRecipeManager}: the server uses {@code getServer().getRecipeManager()} (null on the client, so calling
   * it client-side NPEs), while the client reads the synced {@link modernmods.mantle.recipe.sync.ClientRecipeCache}
   * (MATERIAL is registered syncable). Used by the tinker station / part builder which resolve materials on both sides.
   */
  @javax.annotation.Nullable
  public static MaterialRecipe getRecipe(ISingleStackContainer inv, Level world) {
    if (world.isClientSide()) {
      for (MaterialRecipe recipe : modernmods.mantle.recipe.helper.RecipeHelper.getRecipes(modernmods.mantle.recipe.sync.ClientRecipeCache.getRecipeMap(), TinkerRecipeTypes.MATERIAL.get(), MaterialRecipe.class)) {
        if (recipe.matches(inv, world)) {
          return recipe;
        }
      }
      return null;
    }
    return world.getServer().getRecipeManager().getRecipeFor(TinkerRecipeTypes.MATERIAL.get(), inv, world).map(net.minecraft.world.item.crafting.RecipeHolder::value).orElse(null);
  }

  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    if (ingredient != null) {
      list.add(ingredient);
    }
    return list;
  }

  /** Cache of the display items list */
  private List<ItemStack> displayItems = null;

  /** Gets a list of stacks for display in the recipe */
  public List<ItemStack> getDisplayItems() {
    if (displayItems == null) {
      if (needed > 1) {
        displayItems = Arrays.stream(ingredient.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new))
                             .map(stack -> stack.copyWithCount(needed))
                             .collect(Collectors.toList());
      } else {
        displayItems = Arrays.asList(ingredient.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new));
      }
    }
    return displayItems;
  }

  /**
   * Gets the amount to repair per item for tool repair
   * @param amount  Base material amount, typically the head durability stat
   * @return  Float amount per item to repair
   */
  public float scaleRepair(float amount) {
    // not cached as it may vary per stat type
    return this.getValue() * amount / INGOTS_PER_REPAIR / this.getNeeded();
  }
}
