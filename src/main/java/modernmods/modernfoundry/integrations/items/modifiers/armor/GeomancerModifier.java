package modernmods.modernfoundry.integrations.items.modifiers.armor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import modernmods.modernfoundry.integrations.common.capabilities.ArsElementalSet;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.util.ArsElementalClientHelper;
import modernmods.modernfoundry.integrations.util.ArsElementalHelper;

public class GeomancerModifier extends ArsElementalSetBase implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.MODIFY_DAMAGE, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public boolean hasArmorSet() {
        return ArsElementalClientHelper.hasEarthArmorSet();
    }

    @Override
    public void setHasSet(ArsElementalSet data, boolean hasSet) {
        data.setEarth(hasSet);
    }

    @Override
    public ModifierId getModifierId() {
        return TciModifiers.GEOMANCER_MODIFIER.getId();
    }

    @Override
    public boolean hasArmorSet(Player player) {
        return ArsElementalHelper.hasEarthArmorSet(player);
    }


    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity entity, int slot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
       final Player player = entity instanceof Player ? (Player) entity : null;

        if (player != null && !player.level().isClientSide && hasArmorSet(player) && player.getEyePosition().y() < 20 && player.getFoodData().getFoodLevel() < 2) {
            player.getFoodData().setFoodLevel(20);
        }
    }

}


