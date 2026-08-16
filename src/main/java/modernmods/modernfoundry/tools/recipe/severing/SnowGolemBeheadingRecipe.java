package modernmods.modernfoundry.tools.recipe.severing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.recipe.ingredient.EntityIngredient;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.tools.TinkerModifiers;

/** Beheading recipe to drop pumpkins only if equipped */
public class SnowGolemBeheadingRecipe extends SeveringRecipe {
  public static final RecordLoadable<SnowGolemBeheadingRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), BASE_CHANCE_FIELD, LOOTING_BONUS_FIELD, SnowGolemBeheadingRecipe::new);

  public SnowGolemBeheadingRecipe(ResourceLocation id, float baseChance, float lootingBonus) {
    super(id, EntityIngredient.of(EntityType.SNOW_GOLEM), ItemOutput.fromItem(Items.CARVED_PUMPKIN), baseChance, lootingBonus);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.snowGolemBeheadingSerializer.get();
  }

  @Override
  public ItemStack getOutput(Entity entity) {
    if (entity instanceof SnowGolem && !((SnowGolem)entity).hasPumpkin()) {
      return new ItemStack(Blocks.SNOW_BLOCK);
    }
    return getOutput().copy();
  }
}
