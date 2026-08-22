package modernmods.modernfoundry.integrations.data;

import modernmods.modernfoundry.TConstruct;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import net.neoforged.neoforge.common.crafting.conditions.IConditionBuilder;

import modernmods.hilt.recipe.data.IRecipeHelper;


public abstract class BaseRecipeProvider extends RecipeProvider implements IConditionBuilder, IRecipeHelper {

    public BaseRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected abstract void buildRecipes(Consumer<FinishedRecipe> consumer);

    @Override
    public abstract String getName();

    @Override
    public String getModId() {
        return TConstruct.MOD_ID;
    }

}
