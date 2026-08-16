package modernmods.modernfoundry.library.recipe.tinkerstation.repairing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.RecipeResult;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IncrementalModifierRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tools.TinkerModifiers;

@RequiredArgsConstructor
public class ModifierRepairTinkerStationRecipe implements ITinkerStationRecipe, IModifierRepairRecipe {
  public static final RecordLoadable<ModifierRepairTinkerStationRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), MODIFIER_FIELD, INGREDIENT_FIELD, REPAIR_AMOUNT_FIELD, ModifierRepairTinkerStationRecipe::new);

  @Getter
  private final ResourceLocation id;
  @Getter
  private final ModifierId modifier;
  @Getter
  private final Ingredient ingredient;
  @Getter
  private final int repairAmount;

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    ItemStack tinkerable = inv.getTinkerableStack();
    if (!tinkerable.is(TinkerTags.Items.DURABILITY)) {
      return false;
    }
    ToolStack tool = inv.getTinkerable();
    if (tool.getModifierLevel(modifier) == 0) {
      return false;
    }
    return IncrementalModifierRecipe.containsOnlyIngredient(inv, ingredient);
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, RegistryAccess access) {
    ToolStack tool = inv.getTinkerable();
    int amountPerItem = tool.getModifierLevel(modifier) * repairAmount;
    if (amountPerItem <= 0) {
      return RecipeResult.pass();
    }

    // apply modifiers to possibly boost it
    float repairFactor = 1;
    for (ModifierEntry entry : tool.getModifierList()) {
      repairFactor = entry.getHook(ModifierHooks.REPAIR_FACTOR).getRepairFactor(tool, entry, repairFactor);
      if (repairFactor <= 0) {
        return RecipeResult.pass();
      }
    }
    amountPerItem *= repairFactor;

    // get the max amount we can repair
    int available = IncrementalModifierRecipe.getAvailableAmount(inv, ingredient, amountPerItem);
    if (available <= 0) {
      return RecipeResult.pass();
    }
    // we will just repair the max possible here, no reason to try less
    tool = tool.copy();
    ToolDamageUtil.repair(tool, available);
    return LazyToolStack.successCopy(tool, 1, inv.getTinkerableStack());
  }

  @Override
  public int shrinkToolSlotBy() {
    return 1;
  }

  @Override
  public void updateInputs(LazyToolStack result, IMutableTinkerStationContainer inv, boolean isServer) {
    ToolStack tool = inv.getTinkerable();

    // rescale the amount based on modifiers
    float repairFactor = 1.0f;
    for (ModifierEntry entry : tool.getModifierList()) {
      repairFactor = entry.getHook(ModifierHooks.REPAIR_FACTOR).getRepairFactor(tool, entry, repairFactor);
      if (repairFactor <= 0) {
        return;
      }
    }
    // also scale by relevant modifier level
    int amountPerItem = (int)(tool.getModifierLevel(modifier) * repairAmount * repairFactor);
    if (amountPerItem < 0) {
      return;
    }
    // how much do we need to subtract from our inputs still
    int repairRemaining = tool.getDamage() - result.getTool().getDamage();
    IncrementalModifierRecipe.updateInputs(inv, ingredient, repairRemaining, amountPerItem, ItemStack.EMPTY);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.modifierRepair.get();
  }
}
