package modernmods.modernfoundry.library.recipe.modifiers;

import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.common.IngredientLoadable;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.ICustomOutputRecipe;
import modernmods.modernfoundry.library.json.IntRange;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.library.tools.SlotType.SlotCount;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tools.TinkerModifiers;

/**
 * Shared logic for main types of salvage recipes
 */
public class ModifierSalvage implements ICustomOutputRecipe<RecipeInput> {
  public static final RecordLoadable<ModifierSalvage> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    IngredientLoadable.DISALLOW_EMPTY.requiredField("tools", r -> r.toolIngredient),
    IntLoadable.FROM_ONE.defaultField("max_tool_size", ITinkerStationRecipe.DEFAULT_TOOL_STACK_SIZE, r -> r.maxToolSize), // TODO 1.20: max tool size is unused, remove it
    ModifierId.PARSER.requiredField("modifier", r -> r.modifier),
    ModifierEntry.VALID_LEVEL.defaultField("level", r -> r.level),
    SlotCount.LOADABLE.requiredField("slots", r -> r.slots),
    // TODO: should this have check_trait_level?
    ModifierSalvage::new);

  @Getter
  protected final Identifier id;
  /** Ingredient determining tools matched by this */
  protected final Ingredient toolIngredient;
  /** Max size of the tool for this modifier. If the tool size is smaller, the salvage bonus will be reduced */
  @Getter
  protected final int maxToolSize;
  /** Modifier represented by this recipe */
  @Getter
  protected final ModifierId modifier;
  /** Level for this to be applicable */
  protected final IntRange level;
  /** Slots restored by this recipe, if null no slots are restored */
  protected final SlotCount slots;

  public ModifierSalvage(Identifier id, Ingredient toolIngredient, int maxToolSize, ModifierId modifier, IntRange level, SlotCount slots) {
    this.id = id;
    this.toolIngredient = toolIngredient;
    this.maxToolSize = maxToolSize;
    this.modifier = modifier;
    this.level = level;
    this.slots = slots;
    ModifierRecipeLookup.addSalvage(this);
  }

  /**
   * Checks if the given tool stack and level are applicable for this salvage
   * @param stack         Tool item stack
   * @param tool          Tool stack instance, for potential extensions
   * @param originalLevel Level to check
   * @return True if this salvage is applicable
   */
  @SuppressWarnings("unused")
  public boolean matches(ItemStack stack, IToolStackView tool, int originalLevel) {
    return this.level.test(originalLevel) && toolIngredient.test(stack);
  }

  /**
   * Updates the tool data in light of removing this modifier
   * @param tool  Tool instance
   */
  public void updateTool(ToolStack tool) {
    tool.getPersistentData().addSlots(slots.type(), slots.count());
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeType<? extends ModifierSalvage> getType() {
    return (RecipeType<? extends ModifierSalvage>)(RecipeType<?>) TinkerRecipeTypes.DATA.get();
  }

  /** @deprecated Use {@link #matches(ItemStack, IToolStackView, int)} */
  @Deprecated
  @Override
  public boolean matches(RecipeInput inv, Level level) {
    return false;
  }

  @Override
  public RecipeSerializer<? extends ModifierSalvage> getSerializer() {
    return TinkerModifiers.modifierSalvageSerializer.get();
  }
}
