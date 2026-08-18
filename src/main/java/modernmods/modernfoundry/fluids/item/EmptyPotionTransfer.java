package modernmods.modernfoundry.fluids.item;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.mantle.fluid.transfer.EmptyFluidWithNBTTransfer;
import modernmods.mantle.recipe.helper.FluidOutput;
import modernmods.mantle.recipe.helper.ItemOutput;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.utils.TagUtil;

/**
 * Fluid transfer info that empties a fluid from an item, copying the fluid's NBT to the stack
 * @deprecated use {@link modernmods.mantle.fluid.transfer.EmptyPotionTransfer}
 */
@Deprecated(forRemoval = true)
public class EmptyPotionTransfer extends EmptyFluidWithNBTTransfer {
  public static final Identifier ID = TConstruct.getResource("empty_potion");
  public EmptyPotionTransfer(Ingredient input, ItemOutput filled, FluidOutput fluid) {
    super(input, filled, fluid);
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    if (PotionUtils.getPotion(stack).is(Potions.WATER)) {
      return new FluidStack(Fluids.WATER, fluid.getAmount());
    }
    FluidStack result = new FluidStack(fluid.get().getFluid(), fluid.getAmount());
    TagUtil.setTag(result, TagUtil.getTag(stack));
    return result;
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = super.serialize(context);
    json.addProperty("type", ID.toString());
    return json;
  }

  /** Unique loader instance */
  public static final JsonDeserializer<EmptyPotionTransfer> DESERIALIZER = new Deserializer<>(EmptyPotionTransfer::new);
}
