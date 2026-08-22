package modernmods.modernfoundry.thinking.common.modifer.defense;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Objects;

public class SculkBreedModifier extends Modifier implements ModifyDamageModifierHook, EquipmentChangeModifierHook, TooltipModifierHook {
    public static final ResourceLocation ATTRIBUTE_BONUS = TConstruct.getResource("sculk_breed");
    private static final Component Boost = TConstruct.makeTranslation("modifier", "sculk_breed.boost");
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE,ModifierHooks.EQUIPMENT_CHANGE,ModifierHooks.TOOLTIP);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        LivingEntity living = context.getEntity();
        AttributeInstance attribute = living.getAttribute(Attributes.MAX_HEALTH);
        float x = (float)(amount * modifier.getLevel() * 0.5);
        if (living.hasEffect(ModEffects.holder(ModEffects.sculk_power)) && !tool.isBroken()) {
            if (attribute.getModifier(ATTRIBUTE_BONUS) == null) {
            attribute.addTransientModifier(new AttributeModifier(ATTRIBUTE_BONUS, x,
                    AttributeModifier.Operation.ADD_VALUE));
            attribute.getModifier(ATTRIBUTE_BONUS).amount();
            }
            if (attribute.getModifier(ATTRIBUTE_BONUS) != null&&x > Objects.requireNonNull(attribute.getModifier(ATTRIBUTE_BONUS)).amount()) {
                attribute.removeModifier(ATTRIBUTE_BONUS);
                attribute.addTransientModifier(new AttributeModifier(ATTRIBUTE_BONUS, x,
                    AttributeModifier.Operation.ADD_VALUE));
            }
        }
        return amount;
    }
    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        // remove boost when boots are removed
        LivingEntity living = context.getEntity();
        IToolStackView newTool = context.getReplacementTool();
        if (newTool == null || newTool.isBroken() || newTool.getModifier(this).getLevel() < modifier.getLevel()) {
            AttributeInstance attribute = living.getAttribute(Attributes.MAX_HEALTH);
            if (attribute.getModifier(ATTRIBUTE_BONUS) != null) {
                attribute.removeModifier(ATTRIBUTE_BONUS);
                if (living.getHealth()>living.getMaxHealth()){
                    living.setHealth(living.getMaxHealth());
                }
            }
        }
    }
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag) {
        float x = 0;
        if (player != null && key == TooltipKey.SHIFT) {
            AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
            if (attribute!= null && attribute.getModifier(ATTRIBUTE_BONUS) != null) {
                x = (float) Objects.requireNonNull(attribute.getModifier(ATTRIBUTE_BONUS)).amount();
            }
            TooltipModifierHook.addFlatBoost(this,Boost,x,tooltip);
        }
    }
}
