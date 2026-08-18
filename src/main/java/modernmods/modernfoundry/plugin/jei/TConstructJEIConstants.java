package modernmods.modernfoundry.plugin.jei;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.recipe.alloying.AlloyRecipe;
import modernmods.modernfoundry.library.recipe.casting.IDisplayableCastingRecipe;
import modernmods.modernfoundry.library.recipe.entitymelting.EntityMeltingRecipe;
import modernmods.modernfoundry.library.recipe.melting.MeltingRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.library.recipe.molding.MoldingRecipe;
import modernmods.modernfoundry.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import modernmods.modernfoundry.library.recipe.partbuilder.Pattern;
import modernmods.modernfoundry.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import modernmods.modernfoundry.library.recipe.worktable.IModifierWorktableRecipe;
import modernmods.modernfoundry.library.tools.SlotType;
import modernmods.modernfoundry.library.tools.SlotType.SlotCount;

public class TConstructJEIConstants {
  public static final Identifier PLUGIN = TConstruct.getResource("jei_plugin");

  // ingredient types
  public static final IIngredientTypeWithSubtypes<Modifier,ModifierEntry> MODIFIER_TYPE = new IIngredientTypeWithSubtypes<>() {
    @Override
    public Class<? extends ModifierEntry> getIngredientClass() {
      return ModifierEntry.class;
    }

    @Override
    public Class<? extends Modifier> getIngredientBaseClass() {
      return Modifier.class;
    }

    @Override
    public Modifier getBase(ModifierEntry ingredient) {
      return ingredient.getModifier();
    }
  };
  public static final IIngredientType<Pattern> PATTERN_TYPE = () -> Pattern.class;
  public static final IIngredientTypeWithSubtypes<SlotType, SlotCount> SLOT_TYPE = new IIngredientTypeWithSubtypes<>() {
    @Override
    public Class<? extends SlotCount> getIngredientClass() {
      return SlotCount.class;
    }

    @Override
    public Class<? extends SlotType> getIngredientBaseClass() {
      return SlotType.class;
    }

    @Override
    public SlotType getBase(SlotCount slots) {
      return slots.type();
    }
  };

  // casting
  public static final RecipeType<IDisplayableCastingRecipe> CASTING_BASIN = type("casting_basin", IDisplayableCastingRecipe.class);
  public static final RecipeType<IDisplayableCastingRecipe> CASTING_TABLE = type("casting_table", IDisplayableCastingRecipe.class);
  public static final RecipeType<MoldingRecipe> MOLDING = type("molding", MoldingRecipe.class);

  // melting
  public static final RecipeType<MeltingRecipe> MELTING = type("melting", MeltingRecipe.class);
  public static final RecipeType<EntityMeltingRecipe> ENTITY_MELTING = type("entity_melting", EntityMeltingRecipe.class);
  public static final RecipeType<AlloyRecipe> ALLOY = type("alloy", AlloyRecipe.class);
  public static final RecipeType<MeltingRecipe> FOUNDRY = type("foundry", MeltingRecipe.class);

  // tinker station
  public static final RecipeType<IDisplayModifierRecipe> MODIFIERS = type("modifiers", IDisplayModifierRecipe.class);
  public static final RecipeType<SeveringRecipe> SEVERING = type("severing", SeveringRecipe.class);
  public static final RecipeType<ToolBuildingRecipe> TOOL_BUILDING = type("tool_recipes", ToolBuildingRecipe.class);

  // part builder
  public static final RecipeType<IDisplayPartBuilderRecipe> PART_BUILDER = type("part_builder", IDisplayPartBuilderRecipe.class);

  // modifier workstation
  public static final RecipeType<IModifierWorktableRecipe> MODIFIER_WORKTABLE = type("worktable", IModifierWorktableRecipe.class);

  private static <T> RecipeType<T> type(String name, Class<T> clazz) {
    return RecipeType.create(TConstruct.MOD_ID, name, clazz);
  }
}
