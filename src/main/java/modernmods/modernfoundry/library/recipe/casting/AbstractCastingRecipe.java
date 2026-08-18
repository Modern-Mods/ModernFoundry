package modernmods.modernfoundry.library.recipe.casting;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.primitive.BooleanLoadable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Shared logic between item and material casting */
public abstract class AbstractCastingRecipe implements ICastingRecipe {
  /* Common fields */
  protected static final LoadableField<Ingredient,AbstractCastingRecipe> CAST_FIELD = IngredientLoadable.ALLOW_EMPTY.nullableField("cast", AbstractCastingRecipe::getCast);
  protected static final LoadableField<Boolean,AbstractCastingRecipe> CAST_CONSUMED_FIELD = BooleanLoadable.INSTANCE.defaultField("cast_consumed", false, false, AbstractCastingRecipe::isConsumed);
  protected static final LoadableField<Boolean,AbstractCastingRecipe> SWITCH_SLOTS_FIELD = BooleanLoadable.INSTANCE.defaultField("switch_slots", false, false, AbstractCastingRecipe::switchSlots);

  @Getter @Nonnull
  private final RecipeType<? extends ICastingRecipe> type;
  @Getter
  private final Identifier id;
  @Getter
  private final String group;
  /** 'cast' item for recipe (doesn't have to be an actual 'cast'); null means no cast */
  @Getter @Nullable
  private final Ingredient cast;
  @Getter
  private final boolean consumed;
  @Getter @Accessors(fluent = true)
  private final boolean switchSlots;

  @SuppressWarnings("unchecked")
  protected AbstractCastingRecipe(RecipeType<?> type, Identifier id, String group, @Nullable Ingredient cast, boolean consumed, boolean switchSlots) {
    this.type = (RecipeType<? extends ICastingRecipe>) type;
    this.id = id;
    this.group = group;
    this.cast = cast;
    this.consumed = consumed;
    this.switchSlots = switchSlots;
  }

  /**
   * Tests whether the given cast-slot stack matches this recipe's cast. The cast is nullable ({@code null} = no cast); a
   * recipe with no cast is cast directly into an empty slot, so a null cast matches only an empty stack. Callers must use
   * this instead of {@code getCast().test(stack)}, which NPEs on a no-cast recipe.
   */
  protected boolean testCast(net.minecraft.world.item.ItemStack stack) {
    return cast == null ? stack.isEmpty() : cast.test(stack);
  }

  /** Display ingredients for this recipe; the cast if present */
  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    if (this.cast != null) {
      list.add(this.cast);
    }
    return list;
  }
}
