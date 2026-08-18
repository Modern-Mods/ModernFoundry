package modernmods.modernfoundry.library.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Standalone single-fluid tank backed by the 26.1 {@link net.neoforged.neoforge.transfer.ResourceHandler} API, exposing
 * the legacy {@link IFluidHandler}/{@link IFluidTank} fill/drain/getFluid convenience. Unlike {@link FluidTankBase} this
 * has no owning block entity, so it is suitable for item tanks and temporary transfer buffers.
 */
public class SimpleFluidResourceTank extends FluidStacksResourceHandler implements IFluidHandler, IFluidTank {
  /** Optional predicate restricting which fluids may be inserted */
  @Nullable
  private Predicate<FluidStack> validator;

  public SimpleFluidResourceTank(int capacity) {
    super(1, capacity);
  }

  /** Sets a predicate restricting which fluids may be filled into this tank */
  public void setValidator(@Nullable Predicate<FluidStack> validator) {
    this.validator = validator;
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    if (validator != null && !resource.isEmpty() && !validator.test(resource.toStack(1))) {
      return false;
    }
    return super.isValid(index, resource);
  }

  @Nonnull
  @Override
  public FluidStack getFluid() {
    return this.stacks.get(0);
  }

  @Override
  public int getFluidAmount() {
    return this.stacks.get(0).getAmount();
  }

  @Override
  public int getCapacity() {
    return this.capacity;
  }

  /** Updates the tank capacity */
  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public boolean isEmpty() {
    return this.stacks.get(0).isEmpty();
  }

  @Override
  public boolean isFluidValid(FluidStack stack) {
    return isValid(0, FluidResource.of(stack));
  }

  /** Directly sets the fluid, bypassing capacity checks */
  public void setFluid(FluidStack stack) {
    this.stacks.set(0, stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    try (Transaction tx = Transaction.openRoot()) {
      int filled = insert(FluidResource.of(resource), resource.getAmount(), tx);
      if (action.execute()) {
        tx.commit();
      }
      return filled;
    }
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    FluidStack current = getFluid();
    if (current.isEmpty() || maxDrain <= 0) {
      return FluidStack.EMPTY;
    }
    try (Transaction tx = Transaction.openRoot()) {
      int drained = extract(FluidResource.of(current), maxDrain, tx);
      if (action.execute()) {
        tx.commit();
      }
      return drained > 0 ? current.copyWithAmount(drained) : FluidStack.EMPTY;
    }
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !FluidResource.of(resource).matches(getFluid())) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return getFluid();
  }

  @Override
  public int getTankCapacity(int tank) {
    return this.capacity;
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return isFluidValid(stack);
  }
}
