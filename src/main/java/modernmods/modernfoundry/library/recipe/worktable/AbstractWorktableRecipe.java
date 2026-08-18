package modernmods.modernfoundry.library.recipe.worktable;

import net.minecraft.core.registries.BuiltInRegistries;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.LoadableField;
import modernmods.mantle.recipe.ingredient.SizedIngredient;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.recipe.ITinkerableContainer;
import modernmods.modernfoundry.library.recipe.modifiers.ModifierRecipeLookup;
import modernmods.modernfoundry.library.recipe.modifiers.adding.ModifierRecipe;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of modifier worktable recipes, taking a list of inputs
 */
@RequiredArgsConstructor
public abstract class AbstractWorktableRecipe implements IModifierWorktableRecipe {
  public static final Ingredient DEFAULT_TOOLS = modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(TinkerTags.Items.MODIFIABLE);
  protected static final LoadableField<Ingredient,AbstractWorktableRecipe> TOOL_FIELD = IngredientLoadable.DISALLOW_EMPTY.defaultField("tools", DEFAULT_TOOLS, true, r -> r.toolRequirement);
  protected static final LoadableField<List<SizedIngredient>,AbstractWorktableRecipe> INPUTS_FIELD = SizedIngredient.LOADABLE.list(1).requiredField("inputs", r -> r.inputs);

  @Getter
  private final Identifier id;
  protected final Ingredient toolRequirement;
  protected final List<SizedIngredient> inputs;

  /* JEI */
  @Nullable
  protected List<ItemStack> tools;

  public AbstractWorktableRecipe(Identifier id, List<SizedIngredient> inputs) {
    this(id, modernmods.modernfoundry.library.recipe.ingredient.LazyTagIngredient.of(TinkerTags.Items.MODIFIABLE), inputs);
  }

  @Override
  public boolean matches(ITinkerableContainer inv, Level world) {
    if (!toolRequirement.test(inv.getTinkerableStack())) {
      return false;
    }
    return ModifierRecipe.checkMatch(inv, inputs);
  }

  @Override
  public List<ModifierEntry> getModifierOptions(@Nullable ITinkerableContainer inv) {
    if (inv == null) {
      return ModifierRecipeLookup.getRecipeModifierList();
    }
    return inv.getTinkerable().getUpgrades().getModifiers();
  }

  @Override
  public void updateInputs(LazyToolStack result, ITinkerableContainer.Mutable inv, ModifierEntry selected, boolean isServer) {
    ModifierRecipe.updateInputs(inv, inputs);
  }


  /* JEI */

  /** Gets a list of tools to display */
  @Override
  public List<ItemStack> getInputTools() {
    if (tools == null) {
      tools = Arrays.stream(toolRequirement.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new)).map(stack -> IModifiableDisplay.getDisplayStack(stack.getItem())).toList();
    }
    return tools;
  }

  @Override
  public List<ItemStack> getDisplayItems(int slot) {
    if (slot < 0 || slot >= inputs.size()) {
      return Collections.emptyList();
    }
    return inputs.get(slot).getMatchingStacks();
  }

  @Override
  public int getInputCount() {
    return inputs.size();
  }
}
