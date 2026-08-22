package modernmods.modernfoundry.thinking.common.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public class DryingRackRecipes implements Recipe<SingleRecipeInput> {
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;
    private final String category;

    public DryingRackRecipes(ItemStack output, NonNullList<Ingredient> recipeItems, String category) {
        this.output = output;
        this.recipeItems = recipeItems;
        this.category = category;
    }

    @Override
    public boolean matches(SingleRecipeInput container, Level level) {
        return !level.isClientSide() && !recipeItems.isEmpty() && recipeItems.get(0).test(container.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }

    @Override
    public String getGroup() {
        return category;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DRYING_RACK.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DRYING_RACK_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DryingRackRecipes> {
        public static final Serializer INSTANCE = new Serializer();
        private static final MapCodec<DryingRackRecipes> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredient").forGetter(recipe -> List.copyOf(recipe.recipeItems)),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                Codec.STRING.optionalFieldOf("category", "misc").forGetter(recipe -> recipe.category)
        ).apply(instance, (ingredients, output, category) -> new DryingRackRecipes(output, NonNullList.copyOf(ingredients), category)));
        private static final StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipes> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<DryingRackRecipes> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipes> streamCodec() {
            return STREAM_CODEC;
        }

        private static DryingRackRecipes fromNetwork(RegistryFriendlyByteBuf buffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buffer.readVarInt(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            return new DryingRackRecipes(output, inputs, buffer.readUtf());
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, DryingRackRecipes recipe) {
            buffer.writeVarInt(recipe.recipeItems.size());
            for (Ingredient ingredient : recipe.recipeItems) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
            buffer.writeUtf(recipe.category);
        }
    }

    public static class Type implements RecipeType<DryingRackRecipes> {
        public static final Type INSTANCE = new Type();
    }
}
