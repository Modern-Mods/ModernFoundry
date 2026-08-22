package modernmods.modernfoundry.thinking.common.integration;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.recipes.DryingRackRecipes;
import modernmods.modernfoundry.thinking.common.register.ModCommonItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static RecipeType<DryingRackRecipes> DryingRackRecipes_TYPE =
            new RecipeType<>(DryingRackRecipesCategory.UID, DryingRackRecipes.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return TConstruct.getResource("jei_plugin");
    }
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DryingRackRecipesCategory(registration.getJeiHelpers().getGuiHelper()));
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<DryingRackRecipes> recipesDryingRack = rm.getAllRecipesFor(DryingRackRecipes.Type.INSTANCE).stream().map(holder -> holder.value()).toList();
        registration.addRecipes(DryingRackRecipes_TYPE,recipesDryingRack);
    }
    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(new ItemStack(ModCommonItems.drying_rack.get()), DryingRackRecipes_TYPE);
    }
}
