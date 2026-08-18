package modernmods.modernfoundry.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.compat.neoforged.neoforge.registries.ForgeRegistries;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.mantle.recipe.ingredient.FluidIngredient;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.utils.TagUtil;

import java.util.Arrays;
import java.util.List;

/** Casting recipe applying a potion to a tool */
public class TippingCastingRecipe extends PotionCastingRecipe {
  protected static final LoadableField<Ingredient, PotionCastingRecipe> TOOL_FIELD = IngredientLoadable.DISALLOW_EMPTY.requiredField("tools", r -> r.bottle);
  public static final RecordLoadable<TippingCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD,
    ModifierId.PARSER.requiredField("modifier", r -> r.modifier),
    TippingCastingRecipe::new);

  private final ModifierId modifier;
  public TippingCastingRecipe(TypeAwareRecipeSerializer<?> serializer, Identifier id, String group, Ingredient tool, FluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(serializer, id, group, tool, fluid, Items.AIR, coolingTime);
    this.modifier = modifier;
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    // must have the modifier to cast
    ItemStack stack = inv.getStack();
    if (super.matches(inv, level) && ModifierUtil.getModifierLevel(stack, modifier) > 0) {
      // must also have a specific potion, it's what we are going to copy
      // but it can't match what is already on the stack
      CompoundTag fluidTag = inv.getFluidTag();
      return fluidTag != null && fluidTag.contains(PotionUtils.TAG_POTION)
        && !ModifierUtil.getPersistentString(stack, modifier.getIdentifier()).equals(fluidTag.getString(PotionUtils.TAG_POTION));
    }
    return false;
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = inv.getStack().copy();
    CompoundTag tag = inv.getFluidTag();
    if (tag != null) {
      ToolStack.from(result).getPersistentData().putString(modifier.getIdentifier(), tag.getStringOr(PotionUtils.TAG_POTION, ""));
    }
    return result;
  }


  /* JEI */

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      // create a list of tools with the modifier
      List<ItemStack> tools = Arrays.stream(bottle.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new))
        .map(stack -> IDisplayModifierRecipe.withModifiers(IModifiableDisplay.getDisplayStack(stack), List.of(new ModifierEntry(modifier, 1))))
        .toList();
      displayRecipes = ForgeRegistries.POTIONS.getValues().stream()
        .filter(potion -> potion != Potions.WATER.value())
        .map(potion -> {
          // add the potion to the tool list
          String id = Loadables.POTION.getString(potion);
          List<ItemStack> results = tools.stream().map(stack -> {
            ToolStack tool = ToolStack.copyFrom(stack);
            tool.getPersistentData().putString(modifier.getIdentifier(), id);
            return tool.copyStack(stack);
          }).toList();
          // add the potion to the fluid
          CompoundTag fluidNBT = new CompoundTag();
          fluidNBT.putString(PotionUtils.TAG_POTION, id);
          // create the recipe
          return new DisplayCastingRecipe(getId(), getType(), tools, fluid.getFluids().stream()
            .map(fluid -> TagUtil.createFluidStack(fluid.getFluid(), fluid.getAmount(), fluidNBT))
            .toList(),
            results, coolingTime, true);
        }).toList();
    }
    return displayRecipes;
  }
}
