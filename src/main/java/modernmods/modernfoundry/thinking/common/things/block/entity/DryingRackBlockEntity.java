package modernmods.modernfoundry.thinking.common.things.block.entity;

import modernmods.modernfoundry.thinking.common.networking.packet.packet.ItemStackSyncS2CPacket;
import modernmods.modernfoundry.thinking.common.recipes.DryingRackRecipes;
import modernmods.modernfoundry.thinking.common.register.ModBlockEntities;
import modernmods.modernfoundry.thinking.common.things.block.DryingRackBlock;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.utils.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.block.entity.HiltBlockEntity;

import java.util.Map;
import java.util.Optional;

public class DryingRackBlockEntity extends HiltBlockEntity {
    public final ItemStackHandler itemStackHandler = new ItemStackHandler(2) {
        @Override
        public int getSlotLimit(int slot) {return 1;}
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> getStackInSlot(1).isEmpty();
                case 1 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private final Map<Direction, WrappedHandler> directionWrappedHandlerMap =
            Map.of(Direction.DOWN, new WrappedHandler(itemStackHandler, (i) -> i == 1, (i, s) -> false),
                    Direction.UP, new WrappedHandler(itemStackHandler, (i) -> i == 1, (index, stack) -> itemStackHandler.isItemValid(0, stack)),
                    Direction.NORTH, new WrappedHandler(itemStackHandler, (i) -> i == 1, (index, stack) -> itemStackHandler.isItemValid(0, stack)),
                    Direction.SOUTH, new WrappedHandler(itemStackHandler, (i) -> i == 1, (index, stack) -> itemStackHandler.isItemValid(0, stack)),
                    Direction.EAST, new WrappedHandler(itemStackHandler, (i) -> i == 1, (index, stack) -> itemStackHandler.isItemValid(0, stack)),
                    Direction.WEST, new WrappedHandler(itemStackHandler, (i) -> i == 1, (index, stack) -> itemStackHandler.isItemValid(0, stack)));
    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 2998;
    public DryingRackBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.Drying_Rack.get(), blockPos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index){
                    case 0-> DryingRackBlockEntity.this.progress;
                    case 1->DryingRackBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) {
                switch (index){
                    case 0->DryingRackBlockEntity.this.progress = value;
                    case 1->DryingRackBlockEntity.this.maxProgress = value;
                }
            }
            @Override
            public int getCount() {
                return 2;
            }
        };
    }
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return itemStackHandler;
        }
        if (!directionWrappedHandlerMap.containsKey(side)) {
            return itemStackHandler;
        }
        if (!itemStackHandler.getStackInSlot(0).isEmpty() || !itemStackHandler.getStackInSlot(1).isEmpty()) {
            return directionWrappedHandlerMap.get(Direction.DOWN);
        }
        Direction localDir = this.getBlockState().getValue(DryingRackBlock.FACING);
        if (side == Direction.UP || side == Direction.DOWN) {
            return directionWrappedHandlerMap.get(side);
        }
        return switch (localDir) {
            default -> directionWrappedHandlerMap.get(side.getOpposite());
            case EAST -> directionWrappedHandlerMap.get(side.getClockWise());
            case SOUTH -> directionWrappedHandlerMap.get(side);
            case WEST -> directionWrappedHandlerMap.get(side.getCounterClockWise());
        };
    }
    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory",itemStackHandler.serializeNBT(TagUtil.BUILTIN_LOOKUP));
        nbt.putInt("drying_rack.progress",this.progress);
        super.saveAdditional(nbt);

    }
    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        itemStackHandler.deserializeNBT(TagUtil.BUILTIN_LOOKUP, nbt.getCompound("inventory"));
        this.progress = nbt.getInt("drying_rack.progress");
    }
    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for(int i = 0;i<itemStackHandler.getSlots();i++){
            inventory.setItem(i,itemStackHandler.getStackInSlot(i));
        }
        if (this.level != null) {
            Containers.dropContents(this.level,this.worldPosition,inventory);
        }
    }
    public static void tick(Level level, BlockPos blockPos, BlockState state, DryingRackBlockEntity entity) {
        if (!level.isClientSide) {
            TinkerNetwork.getInstance().sendToClientsAround(new ItemStackSyncS2CPacket(entity.itemStackHandler, blockPos), level, blockPos);
        }
        if(hasRecipe(entity)){
            entity.progress ++ ;
            setChanged(level,blockPos,state);
            if(entity.progress >= entity.maxProgress){
                craftItem(entity);
            }
        }else{
            entity.resetProgress();
            setChanged(level,blockPos,state);
        }
    }
    private void resetProgress() {
        this.progress = 0;
    }
    private static void craftItem(DryingRackBlockEntity entity) {
        Level level = entity.level;
        Optional<RecipeHolder<DryingRackRecipes>> recipe = getRecipe(entity);
        if(level != null && hasRecipe(entity) && recipe.isPresent()){
            entity.itemStackHandler.extractItem(0,1,false);
            entity.itemStackHandler.setStackInSlot(1, recipe.get().value().getResultItem(level.registryAccess()));
            entity.resetProgress();
        }
    }
    private static boolean hasRecipe(DryingRackBlockEntity entity) {
        return !entity.itemStackHandler.getStackInSlot(1).isEmpty() ? false : getRecipe(entity).isPresent();
    }
    private static Optional<RecipeHolder<DryingRackRecipes>> getRecipe(DryingRackBlockEntity entity) {
        Level level = entity.level;
        if (level == null) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(DryingRackRecipes.Type.INSTANCE,
                new SingleRecipeInput(entity.itemStackHandler.getStackInSlot(0)), level);
    }
    public ItemStack getRenderStack(){
        ItemStack itemStack;
        if(itemStackHandler.getStackInSlot(1).isEmpty()){
            itemStack = itemStackHandler.getStackInSlot(0);
        }else{
            itemStack = itemStackHandler.getStackInSlot(1);
        }
        return itemStack;
    }
    public void setHandler(ItemStackHandler itemStackHandler){
        for(int i=0;i<itemStackHandler.getSlots();i++){
            this.itemStackHandler.setStackInSlot(i,itemStackHandler.getStackInSlot(i));
        }
    }

}
