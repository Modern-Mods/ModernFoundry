package modernmods.modernfoundry.thinking.common.things.block.entity;

import modernmods.modernfoundry.thinking.common.register.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public class WasteFluidCylinderBlockEntity extends BlockEntity {
    //The code in Da-Technomancer's CrossRoads mod was used.
    //https://github.com/Crossroads-Development/Crossroads/blob/1.20.1/src/main/java/com/Da_Technomancer/crossroads/blocks/fluid/FluidVoidTileEntity.java
    public WasteFluidCylinderBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModBlockEntities.Waste_Fluid_Cylinder.get(), p_155229_, p_155230_);
    }
    private final IFluidHandler fluidHandler = new VoidHandler();

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    private static class VoidHandler implements IFluidHandler{

        @Override
        public int getTanks(){
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank){
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank){
            return 10000;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action){
            return resource.getAmount();
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action){
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action){
            return FluidStack.EMPTY;
        }
    }
}
