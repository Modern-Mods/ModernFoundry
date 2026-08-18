package modernmods.modernfoundry.tools.recipe.severing;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.cow.MushroomCow.Variant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.ingredient.EntityIngredient;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.tools.TinkerModifiers;

/**
 * Recipe to deshroom a mooshroom, taking brown into account
 */
public class MooshroomDemushroomingRecipe extends SeveringRecipe {
  public static final RecordLoadable<MooshroomDemushroomingRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), BASE_CHANCE_FIELD, LOOTING_BONUS_FIELD, MooshroomDemushroomingRecipe::new);

  public MooshroomDemushroomingRecipe(Identifier id, float baseChance, float lootingBonus) {
    super(id, EntityIngredient.of(EntityType.MOOSHROOM), ItemOutput.fromItem(Items.RED_MUSHROOM, 5), baseChance, lootingBonus);
  }

  @Override
  public RecipeSerializer<? extends MooshroomDemushroomingRecipe> getSerializer() {
    return TinkerModifiers.mooshroomDemushroomingSerializer.get();
  }

  @Override
  public ItemStack getOutput(Entity entity) {
    if (entity instanceof MushroomCow mooshroom) {
      if (!mooshroom.isBaby()) {
        return new ItemStack(mooshroom.getVariant() == Variant.BROWN ? Items.BROWN_MUSHROOM : Items.RED_MUSHROOM, 5);
      }
    }
    return ItemStack.EMPTY;
  }
}
