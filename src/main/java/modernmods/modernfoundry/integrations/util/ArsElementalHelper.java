package modernmods.modernfoundry.integrations.util;

import net.minecraft.world.entity.player.Player;

import modernmods.modernfoundry.integrations.common.capabilities.CapabilityRegistry;

public class ArsElementalHelper {

    public static boolean hasAirArmorSet(Player player) {
        return CapabilityRegistry.arsElemental(player).hasAir();
    }

    public static boolean hasAquaArmorSet(Player player) {
        return CapabilityRegistry.arsElemental(player).hasAqua();
    }

    public static boolean hasEarthArmorSet(Player player) {
        return CapabilityRegistry.arsElemental(player).hasEarth();
    }

    public static boolean hasFireArmorSet(Player player) {
        return CapabilityRegistry.arsElemental(player).hasFire();
    }

}
