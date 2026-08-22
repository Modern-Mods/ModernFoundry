package modernmods.modernfoundry.thinking.common.modifer.defense;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ProtectionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import java.util.List;

public class SculkProtectionModifier extends Modifier implements ProtectionModifierHook, TooltipModifierHook {
    private static final Component Boost = TConstruct.makeTranslation("modifier", "sculk_protection.boost");
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROTECTION, ModifierHooks.TOOLTIP);
    }
    private float getBono(LivingEntity living, ModifierEntry modifier){
        if (living.hasEffect(ModEffects.holder(ModEffects.sculk_power))){
            return Math.min(1.5f, living.getEffect(ModEffects.holder(ModEffects.sculk_power)).duration/1600f) * modifier.getLevel();
        }
        return 0;
    }
    @Override
    public float getProtectionModifier(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float modifierValue) {
        return modifierValue + getBono(context.getEntity(), modifier);
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        if (player!=null) {
            tooltip.add(applyStyle(Component.literal(Util.PERCENT_BOOST_FORMAT.format(getBono(player, modifier)/25f) + " ").append(Boost)));
        }
    }
}
