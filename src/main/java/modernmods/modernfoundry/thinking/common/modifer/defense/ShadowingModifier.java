package modernmods.modernfoundry.thinking.common.modifer.defense;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

public class ShadowingModifier extends Modifier implements TooltipModifierHook, ModifyDamageModifierHook {
    private static final Component Resistance = TConstruct.makeTranslation("modifier", "shadowing.resistance");

    @Override
    public int getPriority() {
        return 75;
    }
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this,  ModifierHooks.TOOLTIP,ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        Level world =context.getEntity().getCommandSenderWorld();
        amount-=((float) (15-world.getBrightness(LightLayer.SKY, context.getEntity().blockPosition())+world.getSkyDarken())/7.5)*modifier.getLevel();
        return amount>0?amount:0;
    }
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        Level world;
        if(player!=null){
            world = player.getCommandSenderWorld();
            float boost = (float) (((float) (15-world.getBrightness(LightLayer.SKY, player.blockPosition())+world.getSkyDarken())/7.5)*modifier.getLevel());
            TooltipModifierHook.addFlatBoost(this, Resistance , boost, tooltip);;
        }
    }

}
