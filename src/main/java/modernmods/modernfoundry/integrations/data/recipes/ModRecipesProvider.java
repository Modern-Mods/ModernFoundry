package modernmods.modernfoundry.integrations.data.recipes;

import modernmods.modernfoundry.TConstruct;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import modernmods.modernfoundry.integrations.data.BaseRecipeProvider;
import modernmods.modernfoundry.integrations.items.TciItems;

public class ModRecipesProvider extends BaseRecipeProvider {

    public ModRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "TciIntegration - Mod Recipes";
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TciItems.BRONZE.getNugget(), 9)
            .requires(TciItems.BRONZE.getIngotTag())
            .unlockedBy("has_bronze_ingot", has(TciItems.BRONZE.getIngotTag()))
            .save(consumer, ResourceLocation.fromNamespaceAndPath(TConstruct.MOD_ID, "bronze_ingot_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TciItems.BRONZE.getIngot(), 1)
            .requires(Ingredient.of(TciItems.BRONZE.getNuggetTag()), 9)
            .unlockedBy("has_bronze_nugget", has(TciItems.BRONZE.getNuggetTag()))
            .save(consumer, ResourceLocation.fromNamespaceAndPath(TConstruct.MOD_ID, "bronze_nuggets_from_ingot"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,TciItems.BRONZE.get(), 1)
            .requires(Ingredient.of(TciItems.BRONZE.getIngotTag()), 9)
            .unlockedBy("has_bronze_ingot", has(TciItems.BRONZE.getIngotTag()))
            .save(consumer, ResourceLocation.fromNamespaceAndPath(TConstruct.MOD_ID, "bronze_block_from_ingots"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,TciItems.BRONZE.getIngot(), 9)
            .requires(TciItems.BRONZE.getBlockItemTag())
            .unlockedBy("has_bronze_ingot", has(TciItems.BRONZE.getIngotTag()))
            .save(consumer, ResourceLocation.fromNamespaceAndPath(TConstruct.MOD_ID, "bronze_ingots_from_block"));
    }

}
