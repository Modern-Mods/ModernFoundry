package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.world.entity.player.Player;

import modernmods.modernfoundry.library.modifiers.ModifierId;

import modernmods.modernfoundry.integrations.common.capabilities.ArsElementalSet;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.util.ArsElementalClientHelper;
import modernmods.modernfoundry.integrations.util.ArsElementalHelper;

public class PyromancerModifier extends ArsElementalSetBase {

    @Override
    public boolean hasArmorSet() {
        return ArsElementalClientHelper.hasFireArmorSet();
    }

    @Override
    public void setHasSet(ArsElementalSet data, boolean hasSet) {
        data.setFire(hasSet);
    }

    @Override
    public ModifierId getModifierId() {
        return TciModifiers.PYROMANCER_MODIFIER.getId();
    }

    @Override
    public boolean hasArmorSet(Player player) {
        return ArsElementalHelper.hasFireArmorSet(player);
    }

}


