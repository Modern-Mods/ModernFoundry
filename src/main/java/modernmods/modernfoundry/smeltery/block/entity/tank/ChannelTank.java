package modernmods.modernfoundry.smeltery.block.entity.tank;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.smeltery.block.entity.ChannelBlockEntity;

import javax.annotation.Nonnull;

/** Tank for channel contents */
public class ChannelTank extends FluidTank {
	private static final String TAG_LOCKED = "locked";

	/**
	 * Amount of fluid that may not be extracted this tick
	 * Essentially, since we cannot guarantee tick order, this prevents us from having a net 0 fluid for the renderer
	 * if draining and filling at the same time
	 */
	private int locked;

	/** Tank owner */
	private final ChannelBlockEntity parent;

	public ChannelTank(int capacity, ChannelBlockEntity parent) {
		super(capacity);
		this.parent = parent;
	}

	/**
	 * Called on channel update to clear the lock, allowing this fluid to be drained
	 */
	public void freeFluid() {
		this.locked = 0;
	}

	/**
	 * Returns the maximum fluid that can be extracted from this tank
	 * @return  Max fluid that can be pulled
	 */
	public int getMaxUsable() {
		return Math.max(fluid.getAmount() - locked, 0);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		boolean wasEmpty = isEmpty();
		int amount = super.fill(resource, action);
		if(action.execute()) {
			locked += amount;
			// if we added something, sync to client
			if (wasEmpty && !isEmpty()) {
				parent.sendFluidUpdate();
			}
		}
		return amount;
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		boolean wasEmpty = isEmpty();
		FluidStack stack = super.drain(maxDrain, action);
		// if we removed something, sync to client
		if (action.execute() && !wasEmpty && isEmpty()) {
			parent.sendFluidUpdate();
		}
		return stack;
	}

	// 26.1.2 FluidTank switched to ValueIO serialization and no longer exposes readFromNBT/writeToNBT(CompoundTag),
	// so serialize the fluid directly under the "Fluid" key (matching FluidTank#serialize) here.
	public FluidTank readFromNBT(CompoundTag nbt) {
		this.locked = nbt.getIntOr(TAG_LOCKED, 0);
		setFluid(TagUtil.readFluid(nbt.getCompoundOrEmpty("Fluid")));
		return this;
	}

	public CompoundTag writeToNBT(CompoundTag nbt) {
		if (!getFluid().isEmpty()) {
			nbt.put("Fluid", TagUtil.writeFluid(getFluid()));
		}
		nbt.putInt(TAG_LOCKED, locked);
		return nbt;
	}
}
