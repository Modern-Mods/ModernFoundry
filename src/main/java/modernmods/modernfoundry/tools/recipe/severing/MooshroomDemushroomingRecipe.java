package modernmods.modernfoundry.tools.recipe.severing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.MushroomCow.MushroomType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.recipe.ingredient.EntityIngredient;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.tools.TinkerModifiers;

/**
 * Recipe to deshroom a mooshroom, taking brown into account
 */
public class MooshroomDemushroomingRecipe extends SeveringRecipe {
  public static final RecordLoadable<MooshroomDemushroomingRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), BASE_CHANCE_FIELD, LOOTING_BONUS_FIELD, MooshroomDemushroomingRecipe::new);

  public MooshroomDemushroomingRecipe(ResourceLocation id, float baseChance, float lootingBonus) {
    super(id, EntityIngredient.of(EntityType.MOOSHROOM), ItemOutput.fromItem(Items.RED_MUSHROOM, 5), baseChance, lootingBonus);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.mooshroomDemushroomingSerializer.get();
  }

  @Override
  public ItemStack getOutput(Entity entity) {
    if (entity instanceof MushroomCow mooshroom) {
      if (!mooshroom.isBaby()) {
        return new ItemStack(mooshroom.getVariant() == MushroomType.BROWN ? Items.BROWN_MUSHROOM : Items.RED_MUSHROOM, 5);
      }
    }
    return ItemStack.EMPTY;
  }
}
