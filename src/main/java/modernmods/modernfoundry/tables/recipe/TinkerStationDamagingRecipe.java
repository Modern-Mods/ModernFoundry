package modernmods.modernfoundry.tables.recipe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.recipe.RecipeResult;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IncrementalModifierRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tables.TinkerTables;

@RequiredArgsConstructor
public class TinkerStationDamagingRecipe implements ITinkerStationRecipe {
  public static final RecordLoadable<TinkerStationDamagingRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
    IntLoadable.FROM_ONE.requiredField("damage_amount", r -> r.damageAmount),
    TinkerStationDamagingRecipe::new);
  private static final RecipeResult<LazyToolStack> BROKEN = RecipeResult.failure(TConstruct.makeTranslationKey("recipe", "damaging.broken"));

  @Getter
  private final Identifier id;
  private final Ingredient ingredient;
  private final int damageAmount;

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    if (!inv.getTinkerableStack().is(TinkerTags.Items.DURABILITY)) {
      return false;
    }
    // must find at least one input, but multiple is fine, as is empty slots
    return IncrementalModifierRecipe.containsOnlyIngredient(inv, ingredient);
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, RegistryAccess access) {
    ToolStack tool = inv.getTinkerable();
    if (tool.isBroken()) {
      return BROKEN;
    }
    // simply damage the tool directly
    tool = tool.copy();
    int maxDamage = IncrementalModifierRecipe.getAvailableAmount(inv, ingredient, damageAmount);
    ItemStack tinkerable = inv.getTinkerableStack();
    ToolDamageUtil.directDamage(tool, maxDamage, null, tinkerable);
    return LazyToolStack.successCopy(tool, 1, tinkerable);
  }

  @Override
  public int shrinkToolSlotBy() {
    return 1;
  }

  @Override
  public void updateInputs(LazyToolStack result, IMutableTinkerStationContainer inv, boolean isServer) {
    // how much did we actually consume?
    int damageTaken = result.getTool().getDamage() - inv.getTinkerable().getDamage();
    IncrementalModifierRecipe.updateInputs(inv, ingredient, damageTaken, damageAmount, ItemStack.EMPTY);
  }

  @Override
  public RecipeSerializer<? extends TinkerStationDamagingRecipe> getSerializer() {
    return TinkerTables.tinkerStationDamagingSerializer.get();
  }
}
