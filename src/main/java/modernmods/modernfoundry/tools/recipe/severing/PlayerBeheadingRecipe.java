package modernmods.modernfoundry.tools.recipe.severing;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.RecipeSerializer;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.helper.ItemOutput;
import modernmods.hilt.recipe.ingredient.EntityIngredient;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.tools.TinkerModifiers;

/** Beheading recipe that sets player skin */
public class PlayerBeheadingRecipe extends SeveringRecipe {
  public static final RecordLoadable<PlayerBeheadingRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), BASE_CHANCE_FIELD, LOOTING_BONUS_FIELD, PlayerBeheadingRecipe::new);
  public PlayerBeheadingRecipe(ResourceLocation id, float baseChance, float lootingBonus) {
    super(id, EntityIngredient.of(EntityType.PLAYER), ItemOutput.fromItem(Items.PLAYER_HEAD), baseChance, lootingBonus);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.playerBeheadingSerializer.get();
  }

  @Override
  public ItemStack getOutput(Entity entity) {
    ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
    if (entity instanceof Player) {
      GameProfile gameprofile = ((Player)entity).getGameProfile();
      stack.set(DataComponents.PROFILE, new ResolvableProfile(gameprofile));
    }
    return stack;
  }
}
