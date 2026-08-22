package modernmods.modernfoundry.thinking.common.register;

import modernmods.modernfoundry.thinking.common.things.block.entity.DryingRackBlockEntity;
import modernmods.modernfoundry.thinking.common.things.block.entity.WasteFluidCylinderBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities extends ModModule {
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> Drying_Rack =
            BLOCK_ENTITIES.register("drying_rack",DryingRackBlockEntity::new, ModCommonItems.drying_rack);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WasteFluidCylinderBlockEntity>> Waste_Fluid_Cylinder =
            BLOCK_ENTITIES.register("waste_fluid_cylinder", WasteFluidCylinderBlockEntity::new, ModCommonItems.waste_fluid_cylinder);

    @SubscribeEvent
    void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Drying_Rack.get(), (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Waste_Fluid_Cylinder.get(), (be, side) -> be.getFluidHandler());
    }
}
