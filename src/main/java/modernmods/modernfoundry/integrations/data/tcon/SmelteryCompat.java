package modernmods.modernfoundry.integrations.data.tcon;

import java.util.Locale;

import lombok.Getter;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.ForgeFlowingFluid;

import modernmods.hilt.registration.object.FluidObject;

import modernmods.modernfoundry.integrations.data.tcon.material.MaterialIds;
import modernmods.modernfoundry.integrations.items.TciItems;

/**
 * Ennum for all generic (multi-mod) materials that need smeltery support
 */
public enum SmelteryCompat {

    DESH (TciItems.MOLTEN_DESH, MaterialIds.desh.getPath()),
    CALORITE (TciItems.MOLTEN_CALORITE, MaterialIds.calorite.getPath()),
    OSTRUM (TciItems.MOLTEN_OSTRUM,MaterialIds.ostrum.getPath());

    @Getter
    private final String name = this.name().toLowerCase(Locale.US);
    private final FluidObject<? extends ForgeFlowingFluid> fluid;
    @Getter
    private final String identifier;

    SmelteryCompat(FluidObject<? extends ForgeFlowingFluid> fluid, String identifier) {
        this.fluid = fluid;
        this.identifier = identifier;
    }

    public FluidObject<?> getFluid() {
        return fluid;
    }

    public Item getBucket() {
        return fluid.asItem();
    }

}
