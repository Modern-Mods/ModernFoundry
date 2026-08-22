package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.world.entity.player.Player;

import modernmods.modernfoundry.library.modifiers.ModifierId;

import modernmods.modernfoundry.integrations.common.capabilities.ArsElementalSet;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.util.ArsElementalClientHelper;
import modernmods.modernfoundry.integrations.util.ArsElementalHelper;

public class AquamancerModifier extends ArsElementalSetBase {

    @Override
    public boolean hasArmorSet() {
        return ArsElementalClientHelper.hasAquaArmorSet();
    }

    @Override
    public void setHasSet(ArsElementalSet data, boolean hasSet) {
        data.setAqua(hasSet);
    }

    @Override
    public ModifierId getModifierId() {
        return TciModifiers.AQUAMANCER_MODIFIER.getId();
    }

    @Override
    public boolean hasArmorSet(Player player) {
        return ArsElementalHelper.hasAquaArmorSet(player);
    }


}


