package modernmods.modernfoundry.thinking.common.modifer.harvest;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import modernmods.modernfoundry.thinking.data.ModDataKeys;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public enum SculkBoostModule implements ModifierModule, TooltipModifierHook , ConditionalStatModifierHook, BreakSpeedModifierHook, EquipmentChangeModifierHook{
    INSTANCE;
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SculkBoostModule>defaultHooks(ModifierHooks.TOOLTIP, ModifierHooks.BREAK_SPEED, ModifierHooks.CONDITIONAL_STAT, ModifierHooks.EQUIPMENT_CHANGE);
    public static final RecordLoadable<SculkBoostModule> LOADER = new SingletonLoader<>(INSTANCE);
    public @NotNull RecordLoadable<SculkBoostModule> getLoader() {
        return LOADER;
    }
    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    private static final Component Boost = TConstruct.makeTranslation("modifier", "sculk_boost");
    public static final ResourceLocation ATTRIBUTE_BONUS = TConstruct.getResource("sculk_boost");
    @Override
    public void onBreakSpeed(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, PlayerEvent.@NotNull BreakSpeed event, @NotNull Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        if (isEffective && event.getEntity().hasEffect(ModEffects.holder(ModEffects.sculk_power))){
            event.setNewSpeed(event.getNewSpeed() * (1 + modifier.getLevel() * 0.2f));
        }
    }
    @Override
    public float modifyStat(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, LivingEntity living, @NotNull FloatToolStat stat, float baseValue, float multiplier) {
        if (living.hasEffect(ModEffects.holder(ModEffects.sculk_power))) {
            if (tool.hasTag(TinkerTags.Items.ARMOR) && stat == ToolStats.ARMOR_TOUGHNESS) {
                AtomicInteger x = new AtomicInteger();
                Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
                dataCap.ifPresent(data -> x.set(data.get(ModDataKeys.SculkBoost, 1)));
                return baseValue * (1 + x.get() * 0.2f);
            }else if (stat == ToolStats.DRAW_SPEED) {
                return baseValue * (1 + (0.2f * modifier.getLevel()));
            }
        }
        return baseValue;
    }
    @Override
    public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        IToolStackView oldTool = context.getOriginalTool();
        if (!tool.isBroken() && (oldTool == null || oldTool.getModifier(modifier.getModifier()).getLevel() != modifier.getLevel())) {
            reset(context.getEntity());
        }
    }
    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        IToolStackView newTool = context.getReplacementTool();
        if (newTool == null || newTool.isBroken() || newTool.getModifier(modifier.getModifier()).getLevel() == 0) {
           reset(context.getEntity());
        }
    }
    private void reset(LivingEntity living){
        AttributeInstance attribute = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (attribute.getModifier(ATTRIBUTE_BONUS) != null) {
            attribute.removeModifier(ATTRIBUTE_BONUS);
        }
        Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
        dataCap.ifPresent(data -> {
            int x = data.get(ModDataKeys.SculkBoost, 0);
            if (x > 0 && living.hasEffect(ModEffects.holder(ModEffects.sculk_power))) living.getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(ATTRIBUTE_BONUS, x * 0.2f,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        );
    }
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        if (player!=null) {
            float bonus = 0;
            if (player.hasEffect(ModEffects.holder(ModEffects.sculk_power))) {
                bonus = 0.2f * modifier.getLevel();
            }
            TooltipModifierHook.addPercentBoost(modifier.getModifier(), Boost, bonus, tooltip);
        }
    }
}
