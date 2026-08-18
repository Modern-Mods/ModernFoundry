package modernmods.modernfoundry.library.recipe.casting.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import modernmods.mantle.fluid.FluidTransferHelper;
import modernmods.mantle.compat.neoforged.neoforge.registries.ForgeRegistries;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.recipe.IMultiRecipe;
import modernmods.mantle.recipe.helper.LoadableRecipeSerializer;
import modernmods.mantle.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.modernfoundry.library.recipe.casting.DisplayCastingRecipe;
import modernmods.modernfoundry.library.recipe.casting.ICastingContainer;
import modernmods.modernfoundry.library.recipe.casting.ICastingRecipe;
import modernmods.modernfoundry.library.utils.TagUtil;

import java.util.Collections;
import java.util.List;

/**
 * Casting recipe that takes an arbitrary fluid for a given amount and fills a container
 */
@RequiredArgsConstructor
public class ContainerFillingRecipe implements ICastingRecipe, IMultiRecipe<DisplayCastingRecipe> {
  public static final RecordLoadable<ContainerFillingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    IntLoadable.FROM_ONE.requiredField("fluid_amount", r -> r.fluidAmount),
    Loadables.ITEM.requiredField("container", r -> r.container),
    ContainerFillingRecipe::new);

  @Getter(lombok.AccessLevel.NONE)
  private final TypeAwareRecipeSerializer<?> serializer;
  @Getter
  private final Identifier id;
  @Getter
  private final String group;
  private final int fluidAmount;
  private final Item container;

  @Override
  @SuppressWarnings("unchecked")
  public RecipeType<? extends ContainerFillingRecipe> getType() {
    return (RecipeType<? extends ContainerFillingRecipe>) serializer.getType();
  }

  @Override
  @SuppressWarnings("unchecked")
  public net.minecraft.world.item.crafting.RecipeSerializer<? extends ContainerFillingRecipe> getSerializer() {
    return (net.minecraft.world.item.crafting.RecipeSerializer<? extends ContainerFillingRecipe>) serializer.serializer();
  }

  @Override
  public int getFluidAmount(ICastingContainer inv) {
    Fluid fluid = inv.getFluid();
    ResourceHandler<FluidResource> handler = ItemAccess.forStack(inv.getStack().copyWithCount(1)).getCapability(Capabilities.Fluid.ITEM);
    return handler == null ? 0 : FluidTransferHelper.fill(handler, new FluidStack(fluid, this.fluidAmount), false);
  }

  @Override
  public boolean isConsumed() {
    return true;
  }

  @Override
  public boolean switchSlots() {
    return false;
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    return 5;
  }

  @Override
  public boolean matches(ICastingContainer inv, Level worldIn) {
    ItemStack stack = inv.getStack();
    // guard empty/wrong item before touching the capability: ItemAccess.forStack throws on an empty stack, and this
    // matches() is called for every faucet pour (including into an empty basin, where the cast slot stack is empty)
    if (stack.isEmpty() || stack.getItem() != this.container.asItem()) {
      return false;
    }
    Fluid fluid = inv.getFluid();
    ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack.copyWithCount(1)).getCapability(Capabilities.Fluid.ITEM);
    return handler != null
           && FluidTransferHelper.fill(handler, new FluidStack(fluid, this.fluidAmount), false) > 0;
  }

  /** @deprecated use {@link ICastingRecipe#assemble(Container, HolderLookup.Provider)} */
    @Deprecated
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return new ItemStack(this.container);
  }

    public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack stack = inv.getStack().copy();
    ItemAccess itemAccess = ItemAccess.forStack(stack.copyWithCount(1));
    ResourceHandler<FluidResource> handler = itemAccess.getCapability(Capabilities.Fluid.ITEM);
    if (handler != null) {
      FluidTransferHelper.fill(handler, TagUtil.createFluidStack(inv.getFluid(), this.fluidAmount, inv.getFluidTag()), true);
      return itemAccess.getResource().toStack(1);
    }
    return stack;
  }

  /* Display */
  /** Cache of items to display for this container */
  private List<DisplayCastingRecipe> displayRecipes = null;

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      List<ItemStack> casts = Collections.singletonList(new ItemStack(container));
      displayRecipes = ForgeRegistries.FLUIDS.getValues().stream()
                                             .filter(fluid -> fluid.getBucket() != Items.AIR && fluid.isSource(fluid.defaultFluidState()))
                                             .map(fluid -> {
                                               FluidStack fluidStack = new FluidStack(fluid, fluidAmount);
                                               ItemStack stack = new ItemStack(container);
                                               stack = FluidUtil.getFluidHandler(stack).map(handler -> {
                                                 handler.fill(fluidStack, FluidAction.EXECUTE);
                                                 return handler.getContainer();
                                               }).orElse(stack);
                                               return new DisplayCastingRecipe(getId(), getType(), casts, Collections.singletonList(fluidStack), stack, 5, true);
                                             })
                                             .toList();
    }
    return displayRecipes;
  }
}
