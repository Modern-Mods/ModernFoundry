package modernmods.modernfoundry.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.compat.neoforged.neoforge.registries.ForgeRegistries;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.helper.LoadableRecipeSerializer;
import modernmods.hilt.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.hilt.recipe.ingredient.FluidIngredient;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import java.util.Arrays;
import java.util.List;

/** Casting recipe clearing the potion from a tool */
public class TipClearingCastingRecipe extends PotionCastingRecipe {
  public static final RecordLoadable<TipClearingCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TippingCastingRecipe.TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD,
    ModifierId.PARSER.requiredField("modifier", r -> r.modifier),
    TipClearingCastingRecipe::new);

  private final ModifierId modifier;
  public TipClearingCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient tool, FluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(serializer, id, group, tool, fluid, Items.AIR, coolingTime);
    this.modifier = modifier;
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    // must have the modifier to cast
    ItemStack stack = inv.getStack();
    // must have the modifier, and the potion set
    return super.matches(inv, level) && ModifierUtil.getModifierLevel(stack, modifier) > 0 && !ModifierUtil.getPersistentString(stack, modifier).isEmpty();
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = inv.getStack().copy();
    ToolStack.from(result).getPersistentData().remove(modifier);
    return result;
  }


  /* JEI */

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      // create a list of tools with the modifier
      List<ItemStack> tools = Arrays.stream(bottle.getItems())
        .map(stack -> IDisplayModifierRecipe.withModifiers(IModifiableDisplay.getDisplayStack(stack), List.of(new ModifierEntry(modifier, 1))))
        .toList();
      // list of tools with the potion set
      List<ItemStack> toolWithPotion = BuiltInRegistries.POTION.stream()
        .filter(potion -> potion != Potions.WATER.value())
        .flatMap(potion -> {
          String id = Loadables.POTION.getString(potion);
          return tools.stream().map(stack -> {
            ToolStack tool = ToolStack.copyFrom(stack);
            tool.getPersistentData().putString(modifier, id);
            return tool.copyStack(stack);
          });
        }).toList();
      // list of tools without the potion set, want the sizes to match
      List<ItemStack> toolWithoutPotion = ForgeRegistries.POTIONS.getValues().stream()
        .filter(potion -> potion != Potions.WATER.value())
        .flatMap(i -> tools.stream()).toList();
      displayRecipes = List.of(new DisplayCastingRecipe(getId(), getType(), toolWithPotion, fluid.getFluids(), toolWithoutPotion, coolingTime, true));
    }
    return displayRecipes;
  }
}
