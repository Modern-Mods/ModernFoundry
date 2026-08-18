package modernmods.modernfoundry.tools.recipe.severing;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.mantle.recipe.ingredient.EntityIngredient;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.tools.TinkerModifiers;

/** Beheading recipe to drop pumpkins only if equipped */
public class SnowGolemBeheadingRecipe extends SeveringRecipe {
  public static final RecordLoadable<SnowGolemBeheadingRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), BASE_CHANCE_FIELD, LOOTING_BONUS_FIELD, SnowGolemBeheadingRecipe::new);

  public SnowGolemBeheadingRecipe(Identifier id, float baseChance, float lootingBonus) {
    super(id, EntityIngredient.of(EntityType.SNOW_GOLEM), ItemOutput.fromItem(Items.CARVED_PUMPKIN), baseChance, lootingBonus);
  }

  @Override
  public RecipeSerializer<? extends SnowGolemBeheadingRecipe> getSerializer() {
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
